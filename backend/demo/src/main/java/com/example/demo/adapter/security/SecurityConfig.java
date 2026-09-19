package com.example.demo.adapter.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestaurantUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Public ────────────────────────────────────────────
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/otp/**",
                                "/actuator/health",
                                "/uploads/**"
                        ).permitAll()

                        // ── Super Admin only ──────────────────────────────────
                        .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")

                        // ── Admin + Super Admin ───────────────────────────────
                        .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // ── Menu management (Admin & Super Admin write; Employee read) ──
                        .requestMatchers("GET", "/api/menu/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/menu/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // ── Inventory (Admin & Super Admin write; Employee read) ──
                        .requestMatchers("GET", "/api/inventory/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/inventory/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // ── Orders (all roles can create/view; admin can cancel/override) ──
                        .requestMatchers("/api/orders/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers("/api/orders/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "EMPLOYEE")

                        // ── Customers (all roles can view/create) ─────────────
                        .requestMatchers("/api/customers/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "EMPLOYEE")

                        // ── Reports & CRM (Admin + Super Admin) ───────────────
                        .requestMatchers("/api/reports/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers("/api/crm/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // ── Activity Logs (Admin + Super Admin) ───────────────
                        .requestMatchers("/api/logs/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // ── Profile (authenticated) ───────────────────────────
                        .requestMatchers("/api/profile/**").authenticated()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
//        System.out.println(
//                new BCryptPasswordEncoder().encode("password123")
//        );
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173",
                frontendUrl
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}