package com.github.yfge.miniapp;

import com.github.yfge.miniboot.autoconfigure.Autowired;
import com.github.yfge.miniboot.autoconfigure.PostConstruct;
import com.github.yfge.miniboot.autoconfigure.Service;
import com.github.yfge.miniboot.web.bind.annotation.RequestMapping;
import com.github.yfge.miniboot.web.bind.annotation.RequestMethod;
import com.github.yfge.miniboot.web.bind.annotation.RestController;


@RestController
@RequestMapping(path="/")
public class SimpleController {
    @Autowired
    private SimpleService simpleService;

    public SimpleController(){
        System.out.println("the controller is created!");
    }
    @PostConstruct
    public void init(){
        System.out.println("the service Id is :"+this.simpleService.getServiceId());
    }

    @RequestMapping(path="/hello",method = RequestMethod.GET)
    public String getHello(){
        return simpleService.getHelloMessage();
    }
}
