package com.skishop.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CsrfTokenConfig csrfTokenConfig;

    public WebMvcConfig(CsrfTokenConfig csrfTokenConfig) {
        this.csrfTokenConfig = csrfTokenConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("/assets/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(csrfTokenConfig);
    }

    /**
     * When running from the monorepo root, Spring Boot auto-detects
     * src/main/webapp (the old Struts project) as the document root.
     * This causes Jasper to scan Struts TLD files that reference classes
     * not on the classpath. Fix: explicitly point to the new project's webapp.
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> documentRootCustomizer() {
        return factory -> {
            File oldStrutsTld = new File("src/main/webapp/WEB-INF/struts-html.tld");
            File newWebapp = new File("monolith-new/src/main/webapp");
            if (oldStrutsTld.exists() && newWebapp.exists()) {
                factory.setDocumentRoot(newWebapp.getAbsoluteFile());
            }
        };
    }
}
