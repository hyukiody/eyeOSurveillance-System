package com.eyeo.data.config;

import com.eyeo.data.filter.LicenseCheckFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration for Data Core.
 * Registers license validation filter.
 */
@Configuration
public class SecurityConfig {
    
    @Bean
    public FilterRegistrationBean<LicenseCheckFilter> licenseFilter(LicenseCheckFilter filter) {
        FilterRegistrationBean<LicenseCheckFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/stream/*", "/storage/*");
        registration.setName("licenseCheckFilter");
        registration.setOrder(1); // Execute before other filters
        return registration;
    }
}
