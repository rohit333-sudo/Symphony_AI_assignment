package com.org.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── Disable CSRF (REST API — stateless, no session) ───────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── Allow H2 console to render in iframes (uses frameOptions) ─
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // ── URL access rules ──────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // H2 console
                        .requestMatchers("/h2-console/**").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        // App info
                        .requestMatchers("/api/info").permitAll()
                        // All library API endpoints
                        .requestMatchers("/api/**").permitAll()
                        // Everything else
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}