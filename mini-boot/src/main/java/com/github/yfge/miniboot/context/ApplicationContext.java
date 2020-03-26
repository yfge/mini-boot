package com.github.yfge.miniboot.context;

import com.github.yfge.miniboot.autoconfigure.Autowired;
import com.github.yfge.miniboot.autoconfigure.ClassUtils;
import com.github.yfge.miniboot.autoconfigure.PostConstruct;
import com.github.yfge.miniboot.autoconfigure.Service;
import com.github.yfge.miniboot.beans.BeansException;
import com.github.yfge.miniboot.beans.factory.config.BeanPostProcessor;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class ApplicationContext {
    private ConcurrentHashMap<String, Object> beanMap;

    private List<BeanPostProcessor> processorList;

    public ApplicationContext() {
        beanMap = new ConcurrentHashMap<>();
        processorList = new LinkedList<>();
        this.addBean(this);
    }

    public void addBean(Object ob) {
        this.beanMap.put(ob.getClass().getName(), ob);
        if (BeanPostProcessor.class.isInstance(ob)) {
            processorList.add((BeanPostProcessor) ob);
        }
    }

    public Object getBean(String beanName) {
        return this.beanMap.get(beanName);
    }

    public Object getBean(Class beanClass) {
        return this.beanMap.get(beanClass.getName());
    }

    public void updateBean(String beanName,Object ob){
        this.beanMap.replace(beanName,ob);
    }
    public ConcurrentHashMap.KeySetView<String, Object> getAllBeans() {
        return this.beanMap.keySet();
    }

    public Iterator<BeanPostProcessor> getPostBeanProcessors() {
        return processorList.iterator();
    }

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
    public void LoadBeans(Class source) throws BeansException {
        ClassUtils util = new ClassUtils();
        List<String> classNames = util.loadClass(source);
        for (String name : classNames) {
            try {
                var classInfo = Class.forName(name);
                /**
                 * 检查是否为一个bean
                 **/
                if (classInfo.isAnnotation() == false && isNeedToCreate(classInfo) && getBean(classInfo) == null) {
                    this.addBean(createAndInitBean(classInfo));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        getAllBeans().forEach((key) -> {
            var bean = getBean(key);
            getPostBeanProcessors().forEachRemaining((beanPostProcessor -> {
                try {
                    updateBean(key, beanPostProcessor.postProcessBeforeInitialization(bean, key));
                } catch (BeansException e) {
                    System.out.println("exec error"+e.getMessage());
                }
            }));
            for (Method method : bean.getClass().getDeclaredMethods()
            ) {
                if (method.getDeclaredAnnotation(PostConstruct.class) != null) {
                    try {
                        method.invoke(bean);
                    } catch (Exception ex) {
                        System.out.println("exec error"+ex.getMessage());
                    }
                }
            }
            getPostBeanProcessors().forEachRemaining((beanPostProcessor -> {
                try {
                    updateBean(key, beanPostProcessor.postProcessAfterInitialization(getBean(key), key));
                } catch (BeansException e) {
                    System.out.println("exec error"+e.getMessage());
                }
            }));

        });
    }

    private Object createAndInitBean(Class<?> classInfo) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, BeansException {
        /**
         * 得到默认构造函数
         */
        var constructor = classInfo.getConstructor();
        if (constructor != null) {
            /**
             * 创建实例
             */
            System.out.println("Bean " + classInfo.getName() + " is created.");
            var obj = constructor.newInstance();
            /** 保存bean**/
            addBean(obj);
            for (var field : classInfo.getDeclaredFields()) {
                if (field.getDeclaredAnnotation(Autowired.class) != null) {
                    field.setAccessible(true);
                    var fieldBean = getBean(field.getType());
                    if (fieldBean == null) {
                        fieldBean = createAndInitBean(field.getType());
                    }
                    try {
                        field.set(obj, fieldBean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return obj;
        } else {
            throw new BeansException();
        }
    }


}
