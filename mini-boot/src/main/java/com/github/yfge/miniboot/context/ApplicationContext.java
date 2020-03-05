package com.github.yfge.miniboot.context;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationContext {
    private Map<String,Object> beanMap ;
    public ApplicationContext(){
        beanMap=new LinkedHashMap<>();
    }
    public void addBean( Object ob){
        this.beanMap.put(ob.getClass().getName(),ob);
    }

    public Object getBean(String beanName){
        return this.beanMap.get(beanName);
    }
    public Object getBean(Class beanClass){
        return this.beanMap.get(beanClass.getName());
    }

    public Collection<Object> getAllBeans(){
        return this.beanMap.values();
    }
}
