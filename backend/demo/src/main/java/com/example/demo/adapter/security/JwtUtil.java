package com.example.demo.adapter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("roles", userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
//        return buildToken(claims, userDetails.getUsername(), expiration);
        Map<String, Object> claims = new HashMap<>();

        claims.put("roles",
                userDetails.getAuthorities()
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()));

        claims.put("type", "access");

        return buildToken(claims, userDetails.getUsername(), expiration);
    }
    public String generateRefreshToken(String username) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return buildToken(
                claims,
                username,
                refreshExpiration
        );
    }

    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long ttl
    ) {

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttl))
                .and()
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {
        return resolver.apply(extractAllClaims(token));
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String username = extractUsername(token);

        String tokenType = extractClaim(
                token,
                claims -> claims.get("type", String.class)
        );

        return "access".equals(tokenType)
                && username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public boolean isRefreshTokenValid(
            String token,
            String username
    ) {

        String tokenType = extractClaim(
                token,
                claims -> claims.get("type", String.class)
        );

        String tokenUsername = extractUsername(token);

        return "refresh".equals(tokenType)
                && username != null
                && username.equals(tokenUsername)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(
                token,
                Claims::getExpiration
        ).before(new Date());
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }}
