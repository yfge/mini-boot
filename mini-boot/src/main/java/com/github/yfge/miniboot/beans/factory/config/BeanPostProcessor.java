package com.github.yfge.miniboot.beans.factory.config;

import com.github.yfge.miniboot.beans.BeansException;

public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
