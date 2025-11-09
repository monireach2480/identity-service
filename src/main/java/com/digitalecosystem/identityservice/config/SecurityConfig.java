package com.digitalecosystem.identityservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Documentation & Health - Always public
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/health",
                                "/api/v1/health",
                                "/api/v1/sync/health"
                        ).permitAll()

                        // Public endpoints - Registration & Restore flow
                        .requestMatchers(
                                "/api/v1/auth/register",      // OTP generation
                                "/api/v1/auth/verify-otp",    // OTP verification
                                "/api/v1/auth/resend-otp",    // Resend OTP
                                "/api/v1/identity/check",     // Check if identity exists
                                "/api/v1/identity/restore"    // Restore identity from backup
                        ).permitAll()

                        // Protected endpoints - Require authentication after OTP verification
                        .requestMatchers(
                                "/api/v1/identity/register",   // Register DID (after OTP)
                                "/api/v1/identity/backup",     // Create backup
                                "/api/v1/sync/batch"           // Sync operations
                        ).authenticated()  // ← PRODUCTION: Requires JWT token

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                );

        // TODO: Enable Keycloak JWT validation for production
        // Uncomment the line below when Keycloak is configured:
        // .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8081"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}