package com.lex.MitmStore.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.lex.MitmStore.utils.HandleRequest;
import com.lexlang.Requests.proxy.ProxyPara;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

public class Intercept extends HttpProxyIntercept {
	
	/**
	 *缓存相应 
	 */
	private final static LinkedHashMap<String, WebResponse> responseStore= new LinkedHashMap<String, WebResponse>() {
		private static final long serialVersionUID = 1L;
		protected boolean removeEldestEntry(Map.Entry<String, WebResponse> eldest) {
            return size() > 300;
        }
	};
	
	
	/**
	* default max content length size is 8MB
	*/
	private static final int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 1024 * 50;
	
	private int maxContentLength;
	
	public Intercept() {
	    this(DEFAULT_MAX_CONTENT_LENGTH);
	}
	
	public Intercept(int maxContentLength) {
	    this.maxContentLength = maxContentLength;
	}
	
	
   /**
    * 请求前面拦截
    */
   @Override
   public void beforeRequest(Channel clientChannel, HttpContent httpContent,HttpProxyInterceptPipeline pipeline) throws Exception {
	   HttpRequest httpRequest = pipeline.getHttpRequest();
	   HandleRequest request=new HandleRequest(httpRequest,pipeline.getRequestProto().getSsl());
	   String detailUrl=request.getUrl();
	   
	   /**
	    * 屏蔽链接
	    */
	   if(rejectResponseOrNot(detailUrl)){
		   clientChannel.close();
		   return ;
	   }
	   
	   if(responseStore.containsKey(detailUrl)){
		   System.out.println("缓存:"+detailUrl);
		   WebResponse webResponse = responseStore.get(detailUrl);
	       //消息体
	       HttpContent httpBody = new DefaultLastHttpContent();
	       httpBody.content().writeBytes(webResponse.getHttpBody());
		   
		   flushStore(clientChannel,webResponse.getHttpHeader(),httpBody);
	   }else{
		   List<Entry<String, String>> hds = request.getHeaders();
		   for(int index=0;index<hds.size();index++){
			   Entry<String, String> hd = hds.get(index);
			   if(hd.getKey().equals("Proxy")){
				   
				   System.out.println("代理访问:"+detailUrl);
				   if(request.getMethod().equals("POST")){
					   request.setBody(httpContent);
				   }
				   //手工代理
				   String[] proxy=hd.getValue().split(":");
				   WebProxy webProxy = new WebProxy(request,new ProxyPara(proxy[0],Integer.parseInt(proxy[1])));
				   WebResponse webResponse = webProxy.visit();
				   
				   if(modifyResponseOrNot(detailUrl)){
					   webResponse.setHttpBody(modifyResponse(new String(webResponse.getHttpBody())).getBytes());
				   }
				   
				   if(storeResponseOrNot(detailUrl)){
				       responseStore.put(detailUrl,webResponse);
				   }
				   
			       //消息体
			       HttpContent httpBody = new DefaultLastHttpContent();
			       httpBody.content().writeBytes(webResponse.getHttpBody());
				   
				   flushStore(clientChannel,webResponse.getHttpHeader(),httpBody);
			   }
		   }
		   pipeline.beforeRequest(clientChannel, httpContent);
	   }
	   
   }
	
	@Override
	public final void afterResponse(Channel clientChannel, Channel proxyChannel,HttpResponse httpResponse,HttpProxyInterceptPipeline pipeline) throws Exception {
	    if (httpResponse instanceof FullHttpResponse) {
	      FullHttpResponse fullHttpResponse = (FullHttpResponse) httpResponse;
	      handelResponse(pipeline.getHttpRequest(), fullHttpResponse, pipeline);
	      if (fullHttpResponse.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
	        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
	      }
	      proxyChannel.pipeline().remove("decompress");
		  proxyChannel.pipeline().remove("aggregator");
		} else if (matchHandle(pipeline.getHttpRequest(), pipeline.getHttpResponse(), pipeline)) {
		  pipeline.resetAfterHead();
		  proxyChannel.pipeline().addAfter("httpCodec", "decompress", new HttpContentDecompressor());
		  proxyChannel.pipeline().addAfter("decompress", "aggregator", new HttpObjectAggregator(maxContentLength));
		  proxyChannel.pipeline().fireChannelRead(httpResponse);
		  return;
		}
	    pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
	}
	
	@Deprecated
	/**
	* 剥离到工具类中了：{@link com.github.monkeywie.proxyee.util#isHtml(HttpRequest, HttpResponse)}
	*/
	protected boolean isHtml(HttpRequest httpRequest, HttpResponse httpResponse) {
	    String accept = httpRequest.headers().get(HttpHeaderNames.ACCEPT);
	    String contentType = httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
	    return httpResponse.status().code() == 200 && accept != null && accept
	            .matches("^.*text/html.*$") && contentType != null && contentType
	    .matches("^text/html.*$");
	}
	
	private boolean matchHandle(HttpRequest httpRequest, HttpResponse httpResponse,HttpProxyInterceptPipeline pipeline) {
	    boolean isMatch = match(httpRequest, httpResponse, pipeline);
	    if (httpRequest instanceof FullHttpRequest) {
	      FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;
	      if (fullHttpRequest.content().refCnt() > 0) {
	        ReferenceCountUtil.release(fullHttpRequest);
	      }
	    }
	    return isMatch;
	}
	
	/**
	* 匹配到的响应会解码成FullResponse
	*/
	public  boolean match(HttpRequest httpRequest, HttpResponse httpResponse,HttpProxyInterceptPipeline pipeline){
		  return true;
	}
	
	/**
	* 拦截并处理响应
	*/
	public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse,HttpProxyInterceptPipeline pipeline) {
	   HandleRequest request=new HandleRequest(httpRequest,pipeline.getRequestProto().getSsl());
	   String detailUrl=request.getUrl();
	   
       if(modifyResponseOrNot(detailUrl)){
    	   httpResponse.content().writeBytes(modifyResponse(byteBufToString(httpResponse.content())).getBytes());
       }
	   
	   if(storeResponseOrNot(detailUrl)){
			//消息头
		   HttpResponse httpHeader = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.valueOf(httpResponse.getStatus().code()));
		   httpHeader.headers().add(pipeline.getHttpResponse().headers());
		   
		   responseStore.put(detailUrl,new WebResponse(detailUrl,httpHeader,byteBufToByte(httpResponse.copy().content())));
	   }
	   
	}
	
	/**
	 * 页面修改注入
	 * @param content
	 * @return
	 */
	public String modifyResponse(String content){
		return content;
	}
	
	private String byteBufToString(ByteBuf bf){
		 byte[] byteArray = new byte[bf.capacity()];
		 bf.readBytes(byteArray); 
		 String result = new String(byteArray);
		 return result;
	}
	
	private byte[] byteBufToByte(ByteBuf bf){
		 byte[] byteArray = new byte[bf.capacity()];
		 bf.readBytes(byteArray); 
		 return byteArray;
	}
	
	
	/**
	 * 数据输出客户端
	 */
	protected void flushStore(Channel clientChannel,HttpResponse httpResponse,HttpContent httpContent){
	    clientChannel.writeAndFlush(httpResponse);
	    clientChannel.writeAndFlush(httpContent);
	    clientChannel.close();
	}
	
	
	/**
     * 屏蔽指定链接
     * @param url
     * @return
     */
    public boolean rejectResponseOrNot(String url){
    	return false;
    }
    
    /**
     * 是否缓存这个Response
     * @param url
     * @return
     */
    public boolean storeResponseOrNot(String url){
    	return false;
    }
    
    /**
     * 是否修改这个Response
     * @param url
     * @return
     */
    public boolean modifyResponseOrNot(String url){
    	return false;
    }
	
}
