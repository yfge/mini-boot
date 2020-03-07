package com.github.yfge.miniboot.server;

import com.github.yfge.miniboot.autoconfigure.UrlMappingInfo;
import com.github.yfge.miniboot.web.servlet.ServerRunnable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerHandler implements HttpHandler {
    private final Map<String, UrlMappingInfo> urlMappingInfoMap;
    private ThreadPoolExecutor poolExecutor ;
    public ServerHandler(Map<String, UrlMappingInfo> urlMappingInfoMap){
        this.urlMappingInfoMap = urlMappingInfoMap;
        poolExecutor= (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        poolExecutor.execute(new ServerRunnable(exchange,urlMappingInfoMap));
    }

}
