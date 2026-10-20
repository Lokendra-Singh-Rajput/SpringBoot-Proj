package com.lokendra.springecom.service;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    
    @Value("${jwt.secret}")
    private String secretKey;

    
    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpiration;

    

    
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claims(Map.of("role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    
    public String generateToken(String username) {
        return generateToken(username, "USER");
    }

    

    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    
    public String extractRole(String token) {
        try {
            Object role = extractClaim(token, claims -> claims.get("role"));
            return role != null ? role.toString() : "USER";
        } catch (Exception e) {
            return "USER";
        }
    }

    

    
    public boolean isTokenValid(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}