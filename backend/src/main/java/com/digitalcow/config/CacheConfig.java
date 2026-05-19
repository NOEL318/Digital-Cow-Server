package com.digitalcow.config;

import com.digitalcow.tenancy.TenantContext;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Configura cache Caffeine con TTL 60s para todos los caches del sistema.
 * Caches: dashboardSummary (Fase 1), catalog-*, health-alerts, dashboard-health (Fase 2),
 * reproduction-alerts, dashboard-reproduction (Fase 3), dashboard-production (Fase 4),
 * dashboard-finance (Fase 5).
 */
@Configuration
public class CacheConfig {

    public static final String DASHBOARD_CACHE = "dashboardSummary";

    /**
     * CacheManager Caffeine con TTL 60s, tamano maximo 1000 y todos los caches registrados.
     *
     * @return CacheManager listo para inyectar
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager();
        mgr.setCacheNames(List.of(
            DASHBOARD_CACHE,
            "catalog-vaccines",
            "catalog-diseases",
            "catalog-medications",
            "catalog-pests",
            "health-alerts",
            "dashboard-health",
            "reproduction-alerts",
            "dashboard-reproduction",
            "dashboard-production",
            "dashboard-finance"
        ));
        mgr.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(60))
            .maximumSize(1000));
        return mgr;
    }

    /**
     * KeyGenerator que produce una key por tenant (accountId).
     * Usado por servicios con caches multi-tenant: health-alerts, dashboard-health.
     *
     * @return KeyGenerator que retorna el accountId actual
     */
    @Bean
    public KeyGenerator tenantKeyGenerator() {
        return (target, method, params) -> {
            Long id = TenantContext.get();
            return id != null ? id : 0L;
        };
    }
}
