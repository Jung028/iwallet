package com.alipay.business.web;

/**
 * @author adam
 * @date 21/4/2026 5:05 PM
 */
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceFilter() {
        FilterRegistrationBean<TraceIdFilter> bean =
                new FilterRegistrationBean<>();

        bean.setFilter(new TraceIdFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(1);

        return bean;
    }
}