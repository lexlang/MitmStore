package com.lex.MitmStore.interceptor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;

import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.lex.MitmStore.utils.HandleRequest;
import com.lexlang.Requests.proxy.ProxyPara;
import com.lexlang.Requests.requests.HttpClientRequests;
import com.lexlang.Requests.responses.Response;

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
* @author lexlang
* @version 2019年6月3日 下午4:38:50
* 
*/
public class WebProxy {
	
	private HandleRequest request;
	private ProxyPara proxyPara;
	
	public WebProxy(HandleRequest request,ProxyPara proxyPara){
		this.request=request;
		this.proxyPara=proxyPara;
	}
	
	public WebResponse visit() throws ClientProtocolException, URISyntaxException, IOException{
		HttpClientRequests requests=new HttpClientRequests(proxyPara);
		request.getHeaders().remove("Proxy");//移除代理这个参数
		
		Response response = null;
		if(request.getMethod().equals("GET")){
			response =requests.getUseHeader(request.getUrl(),listToHeader( request.getHeaders()));
		}else if(request.getMethod().equals("POST")){
			request.getHeaders().remove("Content-Length");//移除长度对post影响
			response =requests.postUseHeader(request.getUrl(), request.getBody(), listToHeader( request.getHeaders()));
		}
		
		//消息头
	   HttpResponse httpHeader = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.valueOf(response.getStatusCode()));

	   List<NameValuePair> hds = response.getHeaders();
	   List<String> cks=new ArrayList<String>();
	   for(int i=0;i<hds.size();i++){
			NameValuePair item = hds.get(i);
			if(item.getName().equals("Set-Cookie")){
				cks.add(item.getValue());
			}else{
				httpHeader.headers().add(item.getName(), item.getValue());
			}
	   }
	   if(cks.size()>0){
			httpHeader.headers().add("Set-Cookie", cks);
	   }
		
	   return new WebResponse(response.getCurrentUrl(),httpHeader,response.getBaos().toByteArray());
	}
	
	private HashMap<String,String> listToHeader(List<Entry<String, String>> hds){
		HashMap<String,String> hm=new HashMap<String,String>();
		for(int index=0;index<hds.size();index++){
			Entry<String, String> item=hds.get(index);
			if(! item.getKey().equals("Content-Length")){
				hm.put(item.getKey(), item.getValue());
			}
		}
		return hm;
	}
	
}
