package com.digitalcow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion principal de Spring Security.
 * - Stateless con JWT (sesion siempre nueva).
 * - CORS por allowlist desde env.
 * - Headers HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy.
 * - Endpoints publicos: /auth/*, /actuator/health, /swagger-ui, /v3/api-docs, breeds.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${digitalcow.security.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Define la cadena de filtros HTTP principal.
     *
     * @param http builder de Spring Security
     * @param jwtFilter filtro que autentica via JWT Bearer
     * @param tenancyFilter filtro que setea TenantContext post-auth
     * @return SecurityFilterChain configurada
     * @throws Exception si falla la construccion
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   com.digitalcow.auth.JwtAuthenticationFilter jwtFilter,
                                                   com.digitalcow.tenancy.TenancyFilter tenancyFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h
                .contentTypeOptions(c -> {})
                .frameOptions(f -> f.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/breeds").permitAll()
                // Solo el endpoint de salud queda publico, el resto del actuator requiere auth para no
                // filtrar metadatos como version o commit a usuarios anonimos.
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(tenancyFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Construye CorsConfigurationSource desde la propiedad allowed-origins.
     *
     * @return fuente de configuracion CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        cfg.setAllowedMethods(List.of("GET","POST","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("X-Trace-Id"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
