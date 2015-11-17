package com.theironyard.configs;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by jessicahuffstutler on 11/17/15.
 */
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) { //serve the public folder
        registry.addResourceHandler("public/**"); //** is everything inside of the public folder including subdirectories
    }
}
