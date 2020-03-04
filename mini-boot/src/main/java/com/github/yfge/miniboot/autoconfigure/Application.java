package com.github.yfge.miniboot.autoconfigure;

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
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void run(Class source) {
        BootApplication a = (BootApplication) source.getAnnotation(BootApplication.class);
        LoadBeans(source);
        System.out.println("The Mini-Boot Application Is Run! The Name is " + a.Name());

    }
}
