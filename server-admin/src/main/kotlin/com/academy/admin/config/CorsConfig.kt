package com.academy.admin.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:18080", "http://localhost:18090")
            .allowedMethods("GET","POST","PATCH","PUT","DELETE","OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
    }
}
