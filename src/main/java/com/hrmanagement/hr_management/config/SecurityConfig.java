package com.hrmanagement.hr_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration tạm thời - cho phép truy cập tất cả static resource.
 * TODO: Sẽ được thay thế bằng JWT-based auth sau khi hoàn thiện backend.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Cho phép tất cả static resources và API auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/pages/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/api/v1/auth/**"
                ).permitAll()
                // Các request khác cần xác thực (sẽ bổ sung JWT sau)
                .anyRequest().permitAll()  // Tạm thời cho phép tất cả để dev frontend
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
