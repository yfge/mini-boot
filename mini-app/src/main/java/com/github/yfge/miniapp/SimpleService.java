package com.github.yfge.miniapp;


import com.github.yfge.miniboot.autoconfigure.Service;

import java.util.UUID;

@Service
public class SimpleService {

    private String serviceId ;
    public SimpleService(){
        serviceId= UUID.randomUUID().toString();
        System.out.println("the service :"+serviceId+" is created!");
    }
    public String getServiceId(){
        return this.serviceId;
    }
}
