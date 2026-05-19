/**
 * Paquete raiz del backend Digital Cow.
 *
 * Define a nivel de paquete el filtro Hibernate global accountFilter usado por todas
 * las entidades multi-tenant. Centralizarlo aqui evita el error "Multiple FilterDef
 * annotations define a filter named accountFilter" que surge cuando varias entidades
 * lo declaran por separado.
 */
@org.hibernate.annotations.FilterDef(
        name = "accountFilter",
        parameters = @org.hibernate.annotations.ParamDef(name = "accountId", type = Long.class)
)
package com.digitalcow;
