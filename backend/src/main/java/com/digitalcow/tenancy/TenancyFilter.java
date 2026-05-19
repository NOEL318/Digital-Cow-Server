package com.digitalcow.tenancy;

import com.digitalcow.auth.JwtAuthenticationFilter.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Despues de Spring Security: setea TenantContext desde el principal JWT.
 * Limpia en finally para no contaminar threads reciclados.
 */
@Component
public class TenancyFilter extends OncePerRequestFilter {

    private final TenantFilterAspect filterAspect;

    public TenancyFilter(TenantFilterAspect filterAspect) { this.filterAspect = filterAspect; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (auth != null && auth.getPrincipal() instanceof AuthPrincipal p) {
                if (p.accountId() != null) {
                    TenantContext.set(p.accountId());
                    MDC.put("accountId", String.valueOf(p.accountId()));
                }
                MDC.put("userId", String.valueOf(p.userId()));
                filterAspect.enableFilterForCurrentSession();
            }
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
            MDC.remove("accountId");
            MDC.remove("userId");
        }
    }
}
