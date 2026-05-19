package com.digitalcow.audit;

import java.lang.annotation.*;

/** Marca un metodo de service para registrar audit log al ejecutarse. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String entityType();
    AuditLog.Action action();
}
