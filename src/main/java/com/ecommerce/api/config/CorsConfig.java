package com.ecommerce.api.config;

import com.ecommerce.api.interceptor.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    @Autowired
    private TenantInterceptor tenantInterceptor;

    /**
     * Configure CORS for Spring Security
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow all origins if "*" is specified, otherwise use the specified origins
        if ("*".equals(allowedOrigins)) {
            config.addAllowedOriginPattern("*");
        } else {
            config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }

        // Set allowed methods
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));

        // Set allowed headers
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // Set exposed headers
        config.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));

        // Set allow credentials
        config.setAllowCredentials(allowCredentials);

        // Set max age
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    /**
     * Configure CORS for Spring MVC
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = "*".equals(allowedOrigins) ? new String[]{"*"} : allowedOrigins.split(",");
                registry.addMapping("/**")
                        .allowedOriginPatterns(origins)
                        .allowedMethods(allowedMethods.split(","))
                        .allowedHeaders("*".equals(allowedHeaders) ? new String[]{"*"} : allowedHeaders.split(","))
                        .exposedHeaders(exposedHeaders.split(","))
                        .allowCredentials(allowCredentials)
                        .maxAge(maxAge);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(tenantInterceptor).addPathPatterns("/**");
            }
        };
    }

    /**
     * Get allowed origins as list
     */
    public List<String> getAllowedOrigins() {
        return Arrays.asList(allowedOrigins.split(","));
    }

    /**
     * Get allowed methods as list
     */
    public List<String> getAllowedMethods() {
        return Arrays.asList(allowedMethods.split(","));
    }

    /**
     * Get allowed headers as list
     */
    public List<String> getAllowedHeaders() {
        return Arrays.asList(allowedHeaders.split(","));
    }

    /**
     * Get exposed headers as list
     */
    public List<String> getExposedHeaders() {
        return Arrays.asList(exposedHeaders.split(","));
    }

    /**
     * Check if origin is allowed
     */
    public boolean isOriginAllowed(String origin) {
        if ("*".equals(allowedOrigins)) {
            return true;
        }
        return getAllowedOrigins().contains(origin);
    }

    /**
     * Check if method is allowed
     */
    public boolean isMethodAllowed(String method) {
        return getAllowedMethods().contains(method.toUpperCase());
    }

    /**
     * Check if header is allowed
     */
    public boolean isHeaderAllowed(String header) {
        if ("*".equals(allowedHeaders)) {
            return true;
        }
        return getAllowedHeaders().contains(header);
    }
}
