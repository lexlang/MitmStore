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
```
* 设置代理样例
```Java
HttpClientRequests requests=new HttpClientRequests(new ProxyPara("127.0.0.1",7777));
Map<String, String> hds = HeaderConfig.getBuilder()
				.setHeader("Proxy", "180.121.133.210:14831")
				.build();
```