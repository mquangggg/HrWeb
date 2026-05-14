package com.hrmanagement.hr_management.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User {

    private OAuth2User oauth2User;
    private String email;

    public CustomOAuth2User(OAuth2User oauth2User, String email) {
        this.oauth2User = oauth2User;
        this.email = email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();// Hàm trả về các thuộc tính của user (VD: name, email, ...)
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();// Hàm trả về các quyền của user
    }

    @Override
    public String getName() {
        // Trả về email để JwtTokenProvide có thể lấy được username
        return email;
    }

    public String getEmail() {
        return email;
    }
}
