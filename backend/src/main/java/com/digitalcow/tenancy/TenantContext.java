package com.digitalcow.tenancy;

/**
 * ThreadLocal con el accountId del request actual.
 * Compatible con virtual threads. Siempre limpiar en finally.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CTX = new ThreadLocal<>();

    private TenantContext() {}

    /** Este metodo guarda el accountId del tenant actual en el hilo. */
    public static void set(Long accountId) { CTX.set(accountId); }
    /** Este metodo devuelve el accountId del tenant del hilo actual. */
    public static Long get() { return CTX.get(); }
    /** Este metodo limpia el accountId del hilo actual. */
    public static void clear() { CTX.remove(); }

    /**
     * Devuelve el accountId actual o lanza si no hay tenant en contexto.
     *
     * @return accountId no nulo
     * @throws IllegalStateException si no hay tenant
     */
    public static Long requireAccountId() {
        Long id = CTX.get();
        if (id == null) throw new IllegalStateException("No tenant in context");
        return id;
    }
}
