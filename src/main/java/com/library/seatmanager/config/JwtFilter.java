package com.library.seatmanager.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        jakarta.servlet.http.HttpServletRequest httpRequest =
                (jakarta.servlet.http.HttpServletRequest) request;

        String header =
                httpRequest.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            if (jwtUtil.validateToken(token)) {

                Claims claims =
                        jwtUtil.getClaims(token);

                String username =
                        claims.getSubject();

                String userType =
                        claims.get("userType", String.class);

                String role =
                        claims.get("role", String.class);

                // =================================================
                // AUTHORITIES
                // =================================================

                List<SimpleGrantedAuthority> authorities =
                        new ArrayList<>();

                // =================================================
                // ADMIN
                // =================================================

                if ("ADMIN".equals(userType)) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "ROLE_ADMIN"
                            )
                    );
                }

                // =================================================
                // EMPLOYEE
                // =================================================

                else if ("EMPLOYEE".equals(userType)) {

                    // ---------------------------------------------
                    // Common employee authority
                    // ---------------------------------------------

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "ROLE_EMPLOYEE"
                            )
                    );

                    // ---------------------------------------------
                    // Actual employee role
                    // ---------------------------------------------

                    if (role != null && !role.isBlank()) {

                        authorities.add(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        );
                    }
                }

                // =================================================
                // CREATE AUTHENTICATION
                // =================================================

                if (!authorities.isEmpty()) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );

                    // =================================================
                    // IMPORTANT
                    // Keep complete JWT claims available to
                    // controllers/services.
                    // =================================================

                    authentication.setDetails(claims);

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }
        }

        chain.doFilter(request, response);
    }
}