package com.github.yfge.miniboot.server;

import com.github.yfge.miniboot.autoconfigure.Autowired;
import com.github.yfge.miniboot.autoconfigure.Service;
import com.github.yfge.miniboot.autoconfigure.UrlMappingInfo;
import com.github.yfge.miniboot.beans.BeansException;
import com.github.yfge.miniboot.beans.factory.config.BeanPostProcessor;
import com.github.yfge.miniboot.context.ApplicationContext;
import com.github.yfge.miniboot.web.bind.annotation.RequestMapping;
import com.github.yfge.miniboot.web.bind.annotation.RestController;


@Service
public class RestApiPostProcessor implements BeanPostProcessor {

    @Autowired
    private ServerHandler serverHandler;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var classInfo = bean.getClass();
        var urlMapping = serverHandler.getUrlMappingInfoMap();
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
                            url = url.replace("//", "/");
                            System.out.println(url + " is Mapping");
                            urlMapping.put(url, new UrlMappingInfo(url, methodRequestMapping.method(), method, bean));
                        }

                    }
                }
            }
        }
        return bean;
    }

}
