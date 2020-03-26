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

            ServerResponseData responseData=null;
            try {
                responseData =  generateResponseData(url, method);
            }catch (Throwable e){

                responseData= new ServerResponseData(500,"Server Internal Error".getBytes());
            }
            exchange.sendResponseHeaders(responseData.getCode(),0);
            exchange.getResponseBody().write(responseData.getData());
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServerResponseData generateResponseData(String url, String method) throws IOException, IllegalAccessException, InvocationTargetException {
        var mappingInfo = this.urlMappingInfoMap.getOrDefault(url, null);
        if (mappingInfo == null) {
            return new ServerResponseData(405,"Not Allowed".getBytes());
        } else {
            for (var allowedMethod : mappingInfo.getRequestMethods()) {
                if (allowedMethod.toString().equals(method)) {
                    var ob = mappingInfo.getMethod().invoke(mappingInfo.getBean());
                    return new ServerResponseData(200,ob.toString().getBytes());
                }
            }
            return new ServerResponseData(405,"Not Allowed".getBytes());

        }
    }
}
