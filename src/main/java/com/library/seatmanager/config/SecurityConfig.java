package com.library.seatmanager.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

private final JwtFilter jwtFilter;

public SecurityConfig(JwtFilter jwtFilter) {
    this.jwtFilter = jwtFilter;
}


@Bean
public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth

                        // Existing Admin/Employee authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Owner signup and login are public endpoints.
                        // Signup itself requires the invitation key.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/owner-auth/signup",
                                "/api/owner-auth/login"
                        ).permitAll()

                        // Owner profile requires a valid Owner token
                        .requestMatchers("/api/owner-auth/me")
                        .hasRole("OWNER")

                        // All Owner Panel APIs require Owner authorization
                        .requestMatchers("/api/owner/**")
                        .hasRole("OWNER")

                        // Existing public enquiry endpoint
                        .requestMatchers("/api/customer-enquiries")
                        .permitAll()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
}



@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
}