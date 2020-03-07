package com.github.yfge.miniboot.autoconfigure;

import com.github.yfge.miniboot.web.bind.annotation.RequestMethod;
import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.Method;

public class UrlMappingInfo {
    private String url;
    private RequestMethod[] requestMethods;
    private Method method;
    private Object bean;
    public UrlMappingInfo(String url,RequestMethod[] requestMethods ,Method method,Object bean){
        this.url=url;
        this.requestMethods = requestMethods;
        this.method = method;
        this.bean=bean;
    }

    public RequestMethod[] getRequestMethods() {
        return requestMethods;
    }
    public Method getMethod(){
        return method;
    }
    public String getUrl(){
        return url;
    }
    public Object getBean(){
        return bean;
    }
}
