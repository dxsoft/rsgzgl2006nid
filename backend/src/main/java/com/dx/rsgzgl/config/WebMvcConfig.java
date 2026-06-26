package com.dx.rsgzgl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiAuthenticationInterceptor apiAuthenticationInterceptor;

    public WebMvcConfig(ApiAuthenticationInterceptor apiAuthenticationInterceptor) {
        this.apiAuthenticationInterceptor = apiAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiAuthenticationInterceptor)
                .addPathPatterns("/api/**");
    }
}
