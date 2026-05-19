package com.digitalcow.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee Authorization: Bearer, parsea JWT y popula SecurityContext.
 * Si el token es invalido, no autentica pero deja pasar (los protected endpoints rechazaran).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtAuthenticationFilter(JwtService jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims c = jwt.parse(header.substring(7));
                String userId = c.getSubject();
                @SuppressWarnings("unchecked")
                List<String> roles = c.get("roles", List.class);
                List<SimpleGrantedAuthority> auths = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .toList();
                var authToken = new UsernamePasswordAuthenticationToken(new AuthPrincipal(
                    Long.valueOf(userId), c.get("accountId", Long.class), c.get("email", String.class)
                ), null, auths);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ignored) {
                // token invalido: dejar pasar sin autenticar
            }
        }
        chain.doFilter(req, res);
    }

    /** Principal con datos del usuario autenticado. */
    public record AuthPrincipal(Long userId, Long accountId, String email) {}
}
