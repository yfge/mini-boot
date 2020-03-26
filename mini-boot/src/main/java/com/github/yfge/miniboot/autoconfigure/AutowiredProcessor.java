package com.github.yfge.miniboot.autoconfigure;

import com.github.yfge.miniboot.beans.BeansException;
import com.github.yfge.miniboot.beans.factory.config.BeanPostProcessor;
import com.github.yfge.miniboot.context.ApplicationContext;


//@Service
public class AutowiredProcessor implements BeanPostProcessor {

    @Autowired
    private ApplicationContext applicationContext;
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
       return bean;
    }

}
