package com.lex.MitmStore.utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.monkeywie.proxyee.util.ProtoUtil;
import com.github.monkeywie.proxyee.util.ProtoUtil.RequestProto;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

/**
* @author lexlang
* @version 2018年12月2日 下午9:26:29
* 
*/
public 	class HandleRequest{
	private String method;
	private String url;
	private String httpVison;
	private String body;
	private List<Entry<String, String>> hds=new ArrayList<Map.Entry<String, String>> ();
	private String host;
	private String port;
	
	
	public HandleRequest(HttpRequest request,boolean isSSL){
		method=request.getMethod().name();
		url=(isSSL?"https://":"http://")+request.headers().get(HttpHeaderNames.HOST)+request.getUri();
		HttpHeaders rhds = request.headers();
		hds = rhds.entries();
	}
	
	public String getContent(HttpContent req){
		ByteBuf bf =req.content(); 
		byte[] byteArray = new byte[bf.capacity()];
		bf.readBytes(byteArray);
		String result = new String(byteArray);
		return result;
	}
	

	public String getUrl(){
		return url;
	}
	
	public String getHost(){
		return host;
	}
	
	public String getPort(){
		return port;
	}
	
	public String getMethod(){
		return method;
	}
	
	public void setMethod(String method){
		this.method=method;
	}
	
	public List<Entry<String, String>> getHeaders(){
		return hds;
	}
	
	public String getBody(){
		return body;
	}
	
	public void setBody(HttpContent httpContent){
		body=getContent(httpContent);
	}

	@Override
	public String toString() {
		return "HandleRequest [method=" + method + ", url=" + url + ", httpVison=" + httpVison + ", body=" + body + ", hds=" + hds + "]";
	}
}

