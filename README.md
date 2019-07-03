# MitmStore
http https拦截 修改 缓存数据，加快采集速度，可以设置二级代理
* 启动样例
```Java
HttpProxyServerConfig config =  new HttpProxyServerConfig();
config.setHandleSsl(true);
new HttpProxyServer()
	.serverConfig(config)
	.proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
		@Override
		public void init(HttpProxyInterceptPipeline pipeline) {
			pipeline.addLast(new CertDownIntercept());  //处理证书下载
			pipeline.addLast(new Intercept() {
				@Override
				public boolean storeResponseOrNot(String url){
					return url.endsWith(".js") || url.endsWith(".css") || url.endsWith(".ico") || url.endsWith(".png");
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
				@Override
				public boolean modifyBeforeResponseOrNot(HandleRequest request){
					//不访问原网站,直接修改数据
					return request.getUrl().contains("bejson.com");
				}
				@Override
				public void modifyBeforeResponse(HandleRequest request,Channel clientChannel){
				   HttpResponse httpHeader = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.valueOf(200));
			       HttpContent httpBody = new DefaultLastHttpContent();
	       		   httpBody.content().writeBytes("hello world".getBytes());
		   		   flushStore(clientChannel,webResponse.getHttpHeader(),httpBody);
				}
			}.setMatchStoreUrls(".+baidu.+")//正则缓存
			);
	}
	})
.start(7777);
```
* 设置代理样例
```Java
HttpClientRequests requests=new HttpClientRequests(new ProxyPara("127.0.0.1",7777));
Map<String, String> hds = HeaderConfig.getBuilder()
				.setHeader("Proxy", "180.121.133.210:14831")
				.build();
```