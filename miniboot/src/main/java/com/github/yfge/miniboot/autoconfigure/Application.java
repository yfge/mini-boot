package com.github.yfge.miniboot.autoconfigure;

import  com.github.yfge.miniboot.autoconfigure.BootApplication;

import java.lang.annotation.Annotation;
import  java.util.List;

public class Application {


    /**
     * 根据一个类得到所在的包下面所有类名
     * @param source
     */
    private static void LoadControllers(Class source){


        ClassUtils util = new ClassUtils();
        List<String> classNames = util.loadClass(source);

        for(String name : classNames){
            System.out.println(name);
        }


    }
    public static void run (Class source){
        BootApplication a =(BootApplication)source.getAnnotation(BootApplication.class);
        LoadControllers(source);
        System.out.println("The Mini-Boot Application Is Run!");

    }
}
