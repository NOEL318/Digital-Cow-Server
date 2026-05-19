package com.digitalcow.dashboard;

import com.digitalcow.animal.event.AnimalChangedEvent;
import com.digitalcow.config.CacheConfig;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Invalida la cache de dashboard cuando cambia un animal. */
@Component
public class DashboardCacheInvalidator {

    private final CacheManager caches;

    public DashboardCacheInvalidator(CacheManager caches) { this.caches = caches; }

    /** Este metodo invalida el cache del dashboard cuando cambia un animal. */
    @EventListener
    public void onAnimalChanged(AnimalChangedEvent ev) {
        var cache = caches.getCache(CacheConfig.DASHBOARD_CACHE);
        if (cache != null && ev.accountId() != null) cache.evict(ev.accountId());
    }
}
