package com.my.fmall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * author:zxy
 * 配置拦截器
 * @create 2021-09-28 12:12
 */
@Configuration //xxx.xml
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private AuthInterceptor authInterceptor;

    //拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
            //配置拦截器
            registry.addInterceptor(authInterceptor).addPathPatterns("/**");
            //添加拦截器
            super.addInterceptors(registry);
    }

}
