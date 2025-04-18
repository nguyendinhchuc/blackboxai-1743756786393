package com.ecommerce.api.config;

import com.ecommerce.api.security.CustomAuthenticationFailureHandler;
import com.ecommerce.api.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.api.security.CustomLogoutSuccessHandler;
import com.ecommerce.api.security.JwtAuthenticationFilter;
import com.ecommerce.api.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private CustomLogoutSuccessHandler logoutSuccessHandler;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and().csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/admin/login?expired=true")
                .and()
                .invalidSessionUrl("/admin/login?invalid=true")
                .and()
            .authorizeRequests()
                // Public routes
                .antMatchers("/", "/home", "/about").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/webjars/**").permitAll()
                
                // Swagger UI
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Admin routes
                .antMatchers("/admin/login", "/admin/logout").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                
                // API routes
                .antMatchers("/api/products/**").permitAll()
                .antMatchers("/api/categories/**").permitAll()
                .antMatchers("/api/banners/**").permitAll()
                
                // Secure all other routes
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .permitAll()
                .and()
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                .logoutSuccessHandler(logoutSuccessHandler)
                .deleteCookies("JSESSIONID", "remember-me")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .permitAll()
                .and()
            .rememberMe()
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 1 day
                .rememberMeParameter("remember-me")
                .userDetailsService(userDetailsService)
                .and()
            .exceptionHandling()
                .accessDeniedPage("/admin/access-denied");

        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
