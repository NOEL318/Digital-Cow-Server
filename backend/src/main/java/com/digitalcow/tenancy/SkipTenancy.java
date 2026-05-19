package com.digitalcow.tenancy;

import java.lang.annotation.*;

/** Marca metodos o servicios que NO deben aplicar el filtro multi-tenant (auth, super-admin). */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipTenancy {}
