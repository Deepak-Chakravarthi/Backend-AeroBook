package com.aerobook.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Slf4j
public class PageableConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

        PageableHandlerMethodArgumentResolver resolver =
                new PageableHandlerMethodArgumentResolver();

        // page size and max pageSize can be adjusted based on the business need
        resolver.setFallbackPageable(PageRequest.of(0, 10));
        resolver.setMaxPageSize(100);
        resolvers.add(resolver);
    }
}
