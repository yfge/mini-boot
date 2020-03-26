package com.github.yfge.miniboot.autoconfigure;

import com.github.yfge.miniboot.beans.BeansException;
import com.github.yfge.miniboot.beans.factory.config.BeanPostProcessor;
import com.github.yfge.miniboot.context.ApplicationContext;
import com.github.yfge.miniboot.server.ServerHandler;
import com.github.yfge.miniboot.web.bind.annotation.RequestMapping;
import com.github.yfge.miniboot.web.bind.annotation.RestController;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Application {
    public static void run(Class source) {
        BootApplication a = (BootApplication) source.getAnnotation(BootApplication.class);
        try {
            ApplicationContext cxt = new ApplicationContext();
            cxt.LoadBeans(source);
        } catch (BeansException e) {
            e.printStackTrace();
        }
        System.out.println("The Mini-Boot Application Is Run! The Name is " + a.Name());

    }
}
