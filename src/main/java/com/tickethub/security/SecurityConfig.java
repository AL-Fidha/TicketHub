package com.tickethub.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.web.cors.CorsUtils::isPreFlightRequest).permitAll()
                // Static frontend & common endpoints — always public
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/assets/**", "/error", "/favicon.ico").permitAll()
                // Auth endpoints — public
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // Movie & theatre browsing — public
                .requestMatchers(HttpMethod.GET, "/api/movies/**", "/api/theatres/**", "/api/seats/**", "/api/shows/**").permitAll()
                // Admin-only endpoints
                .requestMatchers(HttpMethod.POST,   "/api/movies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/movies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else requires login
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
