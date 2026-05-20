package com.digitalcow.tenancy;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener JPA que aplica seguridad multi-tenant en dos puntos:
 *
 * - @PrePersist: inyecta accountId desde TenantContext si la entidad no
 *   lo trae seteado, antes de persistir por primera vez.
 *
 * - @PostLoad: verifica que el accountId de la entidad cargada coincide
 *   con el TenantContext actual. Cierra la fuga conocida de Hibernate
 *   donde @Filter NO se aplica a EntityManager.find / Session.get
 *   (findById de Spring Data JPA usa esa via). Sin este chequeo, un
 *   usuario autenticado podria pasar un id de otro tenant y leer
 *   o modificar la entidad. La verificacion se omite si:
 *     - la entidad no tiene campo accountId (catalogos sin tenant)
 *     - la entidad tiene accountId null (registro global del catalogo)
 *     - el TenantContext es null (endpoint sin tenant: auth, super-admin)
 *
 *   Si hay desajuste, lanza NOT_FOUND para no filtrar la existencia del
 *   registro entre tenants.
 */
public class TenantAwareEntityListener {

    private static final Object MISSING = new Object();
    private static final Map<Class<?>, Object> FIELD_CACHE = new ConcurrentHashMap<>();

    /** Este metodo asigna el accountId del tenant actual antes de persistir la entidad. */
    @PrePersist
    public void onPrePersist(Object entity) {
        Field f = resolveField(entity.getClass());
        if (f == null) return;
        try {
            Object current = f.get(entity);
            if (current == null) {
                Long tid = TenantContext.get();
                if (tid != null) f.set(entity, tid);
            }
        } catch (IllegalAccessException ignored) {
            // no-op: si no es accesible la BD rechazara por NOT NULL si aplica
        }
    }

    /**
     * Este metodo verifica que la entidad cargada pertenece al tenant actual.
     *
     * @param entity entidad recien cargada por Hibernate
     */
    @PostLoad
    public void onPostLoad(Object entity) {
        Long tenantId = TenantContext.get();
        if (tenantId == null) return;
        Field f = resolveField(entity.getClass());
        if (f == null) return;
        try {
            Object value = f.get(entity);
            if (value == null) return;
            Long entityAccountId = (Long) value;
            if (!entityAccountId.equals(tenantId)) {
                throw BusinessException.notFound(ErrorCode.NOT_FOUND,
                    "Resource not found");
            }
        } catch (IllegalAccessException ignored) {
            // no-op
        }
    }

    private static Field resolveField(Class<?> c) {
        Object cached = FIELD_CACHE.get(c);
        if (cached == MISSING) return null;
        if (cached != null) return (Field) cached;
        Field found = findAccountIdField(c);
        FIELD_CACHE.put(c, found == null ? MISSING : found);
        return found;
    }

    private static Field findAccountIdField(Class<?> c) {
        Class<?> cur = c;
        while (cur != null && cur != Object.class) {
            for (Field f : cur.getDeclaredFields()) {
                if ("accountId".equals(f.getName())) {
                    f.setAccessible(true);
                    return f;
                }
            }
            cur = cur.getSuperclass();
        }
        return null;
    }
}
