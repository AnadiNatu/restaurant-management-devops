package com.example.demo.adapter.security;

//import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final RestaurantUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        /*
         * No JWT:
         * Let Spring Security continue. Public endpoints such as
         * /api/auth/login are allowed by SecurityConfig.
         */
        if (authHeader == null || authHeader.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        /*
         * A malformed Authorization header should not become an
         * authenticated request.
         */
        if (!authHeader.startsWith("Bearer ")) {
            log.debug("[JWT] Ignoring malformed Authorization header");
            chain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7).trim();

        if (token.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        try {

            String username = jwtUtil.extractUsername(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    log.debug(
                            "[JWT] Authenticated user={} | authorities={}",
                            username,
                            userDetails.getAuthorities()
                    );
                }
            }

        } catch (Exception ex) {

            /*
             * Never place an invalid JWT into the SecurityContext.
             * Continue the chain; Spring Security will return 401/403
             * according to the protected endpoint.
             */
            SecurityContextHolder.clearContext();

            log.warn(
                    "[JWT] Token validation failed: {}",
                    ex.getMessage()
            );
        }

        chain.doFilter(request, response);
    }
}
