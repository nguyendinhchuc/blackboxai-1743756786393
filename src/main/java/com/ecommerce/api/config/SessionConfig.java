package com.ecommerce.api.config;

import com.ecommerce.api.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import jakarta.servlet.http.HttpSessionListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;

@Configuration
public class SessionConfig {

    @Autowired
    private UserSessionService userSessionService;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSIONID");
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setSameSite("Lax"); // or "Strict" for more security
        return serializer;
    }

    @Bean
    public HttpSessionListener sessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(jakarta.servlet.http.HttpSessionEvent se) {
                // Session creation logic can be added here if needed
            }

            @Override
            public void sessionDestroyed(jakarta.servlet.http.HttpSessionEvent se) {
                String sessionId = se.getSession().getId();
                userSessionService.deactivateSession(sessionId);
            }
        };
    }

    // Additional session-related beans and configurations can be added here
    
    /*
    // Example: Configure session timeout (in seconds)
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addContextCustomizers((context) -> context.setSessionTimeout(30));
        return tomcat;
    }
    */

    /*
    // Example: Configure session persistence
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(dataSource);
        return db;
    }
    */

    /*
    // Example: Configure session fixation protection
    @Bean
    public SecurityContextRepository securityContextRepository() {
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.setAllowSessionCreation(true);
        return repository;
    }
    */
}
