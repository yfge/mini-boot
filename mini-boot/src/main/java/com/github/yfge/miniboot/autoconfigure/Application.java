package com.github.yfge.miniboot.autoconfigure;

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
import java.util.List;
import java.util.Map;

public class Application {


    /**
     * 检查一个类是否需要创建（是否是bean)
     *
     * @param beanClass
     * @return
     */
    private static boolean isNeedToCreate(Class beanClass) {
        var annotations = beanClass.getDeclaredAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (var annotation : annotations) {
                if (annotation.annotationType() == Service.class) {
                    return true;
                } else {
                    if (annotation.annotationType() != Target.class && annotation.annotationType() != Retention.class) {
                        return isNeedToCreate(annotation.annotationType());
                    }
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 加载相应的bean(Service)
     *
     * @param source
     */
    private static void LoadBeans(Class source) {
        ClassUtils util = new ClassUtils();
        List<String> classNames = util.loadClass(source);

        Map<String, UrlMappingInfo> urlMappingInfoMap = new LinkedHashMap<>();

        /** 实例化一个context **/
        ApplicationContext applicationContext = new ApplicationContext();
        for (String name : classNames) {
            try {
                var classInfo = Class.forName(name);
                /**
                 * 检查是否声明了@Service
                 **/
                if (classInfo.isAnnotation() == false && isNeedToCreate(classInfo)) {
                    /**
                     * 得到默认构造函数
                     */
                    var constructor = classInfo.getConstructor();
                    if (constructor != null) {
                        /**
                         * 创建实例
                         */
                        var obj = constructor.newInstance();
                        /** 保存bean**/
                        applicationContext.addBean(obj);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        /** 注入bean **/
        for (Object ob : applicationContext.getAllBeans()) {
            var classInfo = ob.getClass();
            for (var field : classInfo.getDeclaredFields()) {
                if (field.getDeclaredAnnotation(Autowired.class) != null) {
                    field.setAccessible(true);
                    var filedBean = applicationContext.getBean(field.getType());
                    if (filedBean != null) {
                        try {
                            field.set(ob, filedBean);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        /** 执行初始化方法 **/
        for (Object ob : applicationContext.getAllBeans()) {
            var classInfo = ob.getClass();
            if (classInfo.getDeclaredAnnotation(Service.class) != null) {
                for (var method : classInfo.getDeclaredMethods()) {
                    if (method.getDeclaredAnnotation(PostConstruct.class) != null) {
                        try {
                            method.invoke(ob);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        /** 生成地址映射**/
        for (Object ob : applicationContext.getAllBeans()) {
            var classInfo = ob.getClass();
            if (classInfo.getDeclaredAnnotation(RestController.class) != null) {
                var requestMapping = classInfo.getDeclaredAnnotation(RequestMapping.class);
                String[] baseUrl = {};
                if (requestMapping != null) {
                    baseUrl = requestMapping.path();
                }
                if (baseUrl.length == 0) {
                    baseUrl = new String[]{"/"};
                }
                for (var method : classInfo.getDeclaredMethods()) {
                    var methodRequestMapping = method.getDeclaredAnnotation(RequestMapping.class);
                    if (methodRequestMapping != null) {
                        String[] subUrl = methodRequestMapping.path();
                        for (var base : baseUrl
                        ) {
                            for (var sub : subUrl) {
                                String url = base + sub;
                                url = url.replace("//","/");
                                System.out.println(url +" is Mapping");
                                urlMappingInfoMap.put(url, new UrlMappingInfo(url, methodRequestMapping.method(), method,ob));
                            }

                        }
                    }
                }
            }
        }
        //
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new ServerHandler(urlMappingInfoMap));
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void run(Class source) {
        BootApplication a = (BootApplication) source.getAnnotation(BootApplication.class);
        LoadBeans(source);
        System.out.println("The Mini-Boot Application Is Run! The Name is " + a.Name());

    }
}
