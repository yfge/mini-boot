package com.github.yfge.miniboot.autoconfigure;

import com.github.yfge.miniboot.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Application {


    /**
     * 加载相应的bean(Service)
     *
     * @param source
     */
    private static void LoadBeans(Class source) {
        ClassUtils util = new ClassUtils();
        List<String> classNames = util.loadClass(source);
        /** 实例化一个context **/
        ApplicationContext applicationContext = new ApplicationContext();
        for (String name : classNames) {
            try {
                var classInfo = Class.forName(name);
                /**
                 * 检查是否声明了@Service
                 **/
                if (classInfo.getDeclaredAnnotation(Service.class) != null) {
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
    }

    public static void run(Class source) {
        BootApplication a = (BootApplication) source.getAnnotation(BootApplication.class);
        LoadBeans(source);
        System.out.println("The Mini-Boot Application Is Run! The Name is " + a.Name());

    }
}
