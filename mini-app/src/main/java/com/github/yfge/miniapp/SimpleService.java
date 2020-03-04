package com.github.yfge.miniapp;


import com.github.yfge.miniboot.autoconfigure.Service;

@Service
public class SimpleService {
    public SimpleService(){
        System.out.println("the service is created!");
    }
}
