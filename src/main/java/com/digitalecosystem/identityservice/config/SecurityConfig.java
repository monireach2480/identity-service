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

                        // Public endpoints - Complete registration flow
                        .requestMatchers(
                                "/api/v1/auth/register",      // OTP generation
                                "/api/v1/auth/verify-otp",    // OTP verification
                                "/api/v1/auth/resend-otp",    // Resend OTP
                                "/api/v1/identity/check",     // Check if identity exists
                                "/api/v1/identity/register",  // Register DID (FIXED: now public)
                                "/api/v1/identity/restore",   // Restore identity from backup
                                "/api/v1/identity/test-link",
                                "/api/v1/trust-token/setup"   // Trust token setup (FIXED: now public)
                        ).permitAll()
                                // Public endpoints - Add these to your existing permitAll() list
                        .requestMatchers(
                                "/api/v1/did/challenge",      // Get proof-of-control challenge
                                "/api/v1/did/prove-control",  // Verify proof of control
                                "/api/v1/did/status"          // Check DID status (consider if this should be public)
                        ).permitAll()

                                // Protected endpoints - Add these to your authenticated() list
                        .requestMatchers(
                                "/api/v1/did/publish",        // Publish DID
                                "/api/v1/did/unpublish"       // Unpublish DID
                        ).authenticated()
                        //this one
                        // Protected endpoints - Require authentication after registration
                        .requestMatchers(
                                "/api/v1/identity/backup",     // Create backup
                                "/api/v1/sync/batch"           // Sync operations
                        ).authenticated()

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
