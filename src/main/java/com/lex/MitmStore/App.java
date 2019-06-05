package com.lex.MitmStore;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

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
		        	@Override
		            public boolean storeResponseOrNot(String url){
		            	return url.endsWith(".js") || url.endsWith(".css") || url.equals(".ico") || url.endsWith(".png");
		            }
		        	@Override
		            public boolean rejectResponseOrNot(String url){
		            	return url.contains("google.com");
		            }
		        	@Override
		            public boolean modifyResponseOrNot(String url){
		            	return  false;//url.equals("http://www.bejson.com/");
		            }
		        	@Override
		        	public byte[] modifyResponse(byte[] origin){
		        		

  						//修改原始文件
						try {
							String content = new String(origin,"utf-8");
							return content.replaceAll("<script.+?</script>", "").getBytes();//修改原页面
						} catch (UnsupportedEncodingException e) {}
						
						/* //保持数据到本地
		        		try{
			        		InputStream inputStream=new ByteArrayInputStream(origin);
			        		BufferedImage img = ImageIO.read(inputStream);
			        		ImageIO.write(img, "PNG", new File("web.png"));
		        		}catch(Exception ex){}
		        		*/
						
		        		return origin;
		        		
		        	}
		            
		        });
		      }
		    })
	   .start(7777);

	}
    
}
