package com.lex.MitmStore.interceptor;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

public class WebResponse {
	private String url;
	private HttpResponse httpHeader;
	//消息体
	private byte[] httpBody;
	
	public WebResponse(String url,HttpResponse httpHeader,byte[] httpBody){
		this.url=url;
		this.httpHeader=httpHeader;
		this.httpBody=httpBody;
	}
	
	public String getUrl() {
		return url;
	}

	public HttpResponse getHttpHeader() {
		return httpHeader;
	}

	public byte[] getHttpBody() {
		return httpBody;
	}
	
	public void setHttpBody(byte[] httpBody){
		this.httpBody=httpBody;
	}
	
	@Override
	public String toString() {
		return "WebResponse [url=" + url + ", httpHeader=" + httpHeader + ", httpBody=" + httpBody + "]";
	}
}
