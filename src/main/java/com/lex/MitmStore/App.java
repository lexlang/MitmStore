package com.lex.MitmStore;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import com.github.monkeywie.proxyee.proxy.ProxyType;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.lex.MitmStore.interceptor.Intercept;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		HttpProxyServerConfig config =  new HttpProxyServerConfig();
		config.setHandleSsl(true);
		new HttpProxyServer()
		    .serverConfig(config)
		    //.proxyConfig(new ProxyConfig(ProxyType.HTTP,"117.63.122.61",14831))
		    .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
		      @Override
		      public void init(HttpProxyInterceptPipeline pipeline) {
		    	pipeline.addLast(new CertDownIntercept());  //处理证书下载
		        pipeline.addLast(new Intercept() {
		            public boolean storeResponseOrNot(String url){
		            	return url.endsWith(".js") || url.endsWith(".css") || url.equals(".ico") || url.endsWith(".png");
		            }
		            
		            public boolean rejectResponseOrNot(String url){
		            	return url.contains("google.com");
		            }
		            
		            public boolean modifyResponseOrNot(String url){
		            	return  false;//url.equals("http://www.bejson.com/");
		            }
		            
		        	public String modifyResponse(String content){
		        		return "<html><head><title>hello world</title></head><body><h1>hello word</h1></body></html>";
		        	}
		            
		        });
		      }
		    })
	   .start(7777);

	}
    
}
