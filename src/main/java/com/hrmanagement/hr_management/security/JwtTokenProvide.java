package com.hrmanagement.hr_management.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvide {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationMs;

    public String generateToken(Authentication authentication) {
        return generateToken(authentication.getName());
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Tạo key từ secret
        SecretKey key = getKey();

        // JJWT 0.12.x: dùng Jwts.SIG.HS512 thay vì SignatureAlgorithm.HS512
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    private SecretKey getKey() {
        // Tạo SecretKey an toàn từ chuỗi secret
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String getUsernameFromJWT(String token) {
        // JJWT 0.12.x: parser() thay vì parserBuilder()
        // verifyWith() thay vì setSigningKey()
        // parseSignedClaims() thay vì parseClaimsJws()
        // getPayload() thay vì getBody()
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
