package com.tanhua.server.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //注入拦截器类
    @Autowired
    private TokenInterceptor tokenInterceptor;

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截所有的请求
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**")
                //排除拦截的路径
                .excludePathPatterns(new String[]{"/user/login","/user/loginVerification"} );


    }
}
