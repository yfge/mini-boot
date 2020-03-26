package com.github.yfge.miniboot.server;

import com.github.yfge.miniboot.autoconfigure.Autowired;
import com.github.yfge.miniboot.autoconfigure.Service;
import com.github.yfge.miniboot.autoconfigure.UrlMappingInfo;
import com.github.yfge.miniboot.web.servlet.ServerRunnable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@Service
public class ServerHandler implements HttpHandler {

    private Map<String, UrlMappingInfo> urlMappingInfoMap = new LinkedHashMap<>();
    public Map<String, UrlMappingInfo> getUrlMappingInfoMap() {
        return urlMappingInfoMap;
    }
    private ThreadPoolExecutor poolExecutor ;
    public ServerHandler(){
        poolExecutor= (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        poolExecutor.execute(new ServerRunnable(exchange,this.getUrlMappingInfoMap()));
    }
}
