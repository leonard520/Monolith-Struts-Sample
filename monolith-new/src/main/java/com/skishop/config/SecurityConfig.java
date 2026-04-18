package com.skishop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.DispatcherType;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()
                .requestMatchers(
                    antMatcher("/"), antMatcher("/home"),
                    antMatcher("/products"), antMatcher("/product"),
                    antMatcher("/login"), antMatcher("/register"),
                    antMatcher("/password/forgot"), antMatcher("/password/reset"),
                    antMatcher("/cart"), antMatcher("/coupons"), antMatcher("/checkout"),
                    antMatcher("/assets/**"), antMatcher("/error"), antMatcher("/favicon.ico")
                ).permitAll()
                .requestMatchers(
                    antMatcher("/orders/**"), antMatcher("/points"),
                    antMatcher("/account/**"), antMatcher("/logout"),
                    antMatcher("/coupons/apply")
                ).hasAnyRole("USER", "ADMIN")
                .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, resp, authEx) ->
                    resp.sendRedirect("/login"))
            )
            .logout(logout -> logout.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );
        return http.build();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repo = new HttpSessionCsrfTokenRepository();
        repo.setParameterName("_csrfToken");
        repo.setHeaderName("X-CSRF-TOKEN");
        return repo;
    }
}
