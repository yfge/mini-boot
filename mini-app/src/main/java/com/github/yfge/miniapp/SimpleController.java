package com.github.yfge.miniapp;

import com.github.yfge.miniboot.autoconfigure.Autowired;
import com.github.yfge.miniboot.autoconfigure.Service;

@Service
public class SimpleController {
    @Autowired
    private SimpleService simpleService;
    public SimpleController(){
        System.out.println("the controller is created!");
    }
}
