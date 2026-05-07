package com.hrmanagement.hr_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository; // Nơi chứa thông tin về các client đã đăng ký
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver; // Bộ phân giải yêu cầu ủy quyền OAuth2 mặc định
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver; // Bộ phân giải yêu cầu ủy quyền OAuth2
import org.springframework.security.web.SecurityFilterChain; // Filter chuỗi bảo mật
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Filter xác thực người dùng

import com.hrmanagement.hr_management.security.JwtAuthenticationFilter; // Filter JWT
import com.hrmanagement.hr_management.security.OAuth2AuthenticationSuccessHandler;
import com.hrmanagement.hr_management.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // ← Bật để @PreAuthorize có hiệu lực
@RequiredArgsConstructor
public class SecurityConfig {
    // Class này chứa cấu hình bảo mật ứng dụng Spring Boot
    private final JwtAuthenticationFilter jwtAuthFilter; // Filter JWT
    private final AuthenticationProvider authenticationProvider; // Filter xác thực người dùng
    private final CustomOAuth2UserService customOAuth2UserService; // Service xử lý OAuth2
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler; // Handler xử lý OAuth2
    private final ClientRegistrationRepository clientRegistrationRepository; // Repository chứa thông tin về các client đã đăng ký

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Hàm cấu hình chuỗi bảo mật
        http
            .csrf(AbstractHttpConfigurer::disable) // Vô hiệu hóa CSRF, CSRF là cơ chế bảo mật để ngăn chặn tấn công Cross-Site Request Forgery
            // vô hiệu hóa để dev mode, khi deploy production cần phải bật CSRF
            .authorizeHttpRequests(auth -> auth // Cấu hình quyền truy cập
                // Cho phép tất cả static resources và API auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/pages/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/api/v1/auth/**",
                    "/oauth2/**"
                ).permitAll()
                // Các request khác cần xác thực
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .oauth2Login(oauth2 -> oauth2 // Cấu hình OAuth2
                .authorizationEndpoint(authorization -> authorization // Cấu hình điểm phân giải OAuth2
                    .authorizationRequestResolver(authorizationRequestResolver())
                )
                .userInfoEndpoint(userInfo -> userInfo // Cấu hình điểm lấy thông tin người dùng OAuth2
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
            );

        return http.build();
    }

    // Bắt buộc Google hiện màn hình chọn tài khoản
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver = 
            new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        
        resolver.setAuthorizationRequestCustomizer(customizer -> 
            customizer.additionalParameters(params -> params.put("prompt", "select_account")));
            
        return resolver;
    }
}
