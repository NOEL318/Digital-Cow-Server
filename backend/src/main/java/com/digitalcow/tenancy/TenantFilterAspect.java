package com.digitalcow.tenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Activa el Hibernate filter "accountFilter" con el accountId del TenantContext.
 * Se invoca al inicio de cada transaccion vinculada a un request.
 */
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager em;

    /** Activa el filtro en la sesion actual. Llamado por TenancyFilter. */
    public void enableFilterForCurrentSession() {
        Long accountId = TenantContext.get();
        if (accountId == null) return;
        Session session = em.unwrap(Session.class);
        if (session.getEnabledFilter("accountFilter") == null) {
            session.enableFilter("accountFilter").setParameter("accountId", accountId);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    try { session.disableFilter("accountFilter"); } catch (Exception ignored) {}
                }
            });
        }
    }
}
