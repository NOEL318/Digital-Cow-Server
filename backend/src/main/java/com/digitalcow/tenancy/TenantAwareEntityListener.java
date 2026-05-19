package com.digitalcow.tenancy;

import jakarta.persistence.PrePersist;
import java.lang.reflect.Field;

/**
 * Inyecta accountId desde TenantContext en cualquier entity con campo accountId
 * antes de persistir, si no esta seteado.
 */
public class TenantAwareEntityListener {

    /** Este metodo asigna el accountId del tenant actual antes de persistir la entidad. */
    @PrePersist
    public void onPrePersist(Object entity) {
        try {
            Field f = findAccountIdField(entity.getClass());
            if (f == null) return;
            f.setAccessible(true);
            Object current = f.get(entity);
            if (current == null) {
                Long tid = TenantContext.get();
                if (tid != null) f.set(entity, tid);
            }
        } catch (IllegalAccessException ignored) {
            // no-op: si no es accesible, la BD rechazara por NOT NULL
        }
    }

    private Field findAccountIdField(Class<?> c) {
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if ("accountId".equals(f.getName())) return f;
            }
            c = c.getSuperclass();
        }
        return null;
    }
}
