package com.github.yfge.miniboot.web.servlet;

import com.github.yfge.miniboot.autoconfigure.UrlMappingInfo;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ServerRunnable implements Runnable {

    private final HttpExchange exchange;
    private final Map<String, UrlMappingInfo> urlMappingInfoMap;

    public ServerRunnable(HttpExchange exchange, Map<String, UrlMappingInfo> urlMappingInfoMap) {
        this.urlMappingInfoMap = urlMappingInfoMap;
        this.exchange = exchange;
    }

    @Override
    public void run() {
        try {
            String url = this.exchange.getRequestURI().getPath();
            String method = this.exchange.getRequestMethod();
            System.out.println(method + " " + url);

            try {
                var mappingInfo = this.urlMappingInfoMap.getOrDefault(url, null);
                if (mappingInfo == null) {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().write("Error Not Found.".getBytes());
                } else {
                    boolean isAllowed = false;
                    for (var allowedMethod : mappingInfo.getRequestMethods()) {
                        if (allowedMethod.toString().equals(method)) {
                            var ob = mappingInfo.getMethod().invoke(mappingInfo.getBean());
                            exchange.sendResponseHeaders(200, 0);
                            exchange.getResponseBody().write(ob.toString().getBytes());
                            isAllowed=true;
                            break;
                        }
                    }
                    if(isAllowed ==false){
                        exchange.sendResponseHeaders(405,0);
                        exchange.getResponseBody().write("not allowed!".getBytes());
                    }
                }
            }catch (Throwable e){
                exchange.sendResponseHeaders(500,0);
                exchange.getResponseBody().write("internal error".getBytes());
            }
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
