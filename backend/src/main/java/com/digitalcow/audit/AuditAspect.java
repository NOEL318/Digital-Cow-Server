package com.digitalcow.audit;

import com.digitalcow.auth.JwtAuthenticationFilter.AuthPrincipal;
import com.digitalcow.tenancy.TenantContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Este aspecto intercepta los metodos anotados con Auditable y guarda una fila
 * en la tabla audit_log con la accion realizada, el tipo de entidad y el usuario.
 *
 * El guardado de auditoria nunca debe interrumpir la operacion principal, por
 * eso el bloque de auditoria envuelve sus errores en un log de advertencia y
 * los descarta. La auditoria se persiste en una transaccion separada para
 * que un fallo aqui no haga rollback del cambio de negocio que ya se commiteo.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository repo;

    public AuditAspect(AuditLogRepository repo) { this.repo = repo; }

    /**
     * Este metodo ejecuta el metodo objetivo y luego registra la entrada de
     * auditoria correspondiente. Si el guardado de la auditoria falla por
     * cualquier motivo se reporta en el log pero la respuesta al usuario no
     * se ve afectada.
     */
    @Around("@annotation(com.digitalcow.audit.Auditable)")
    public Object record(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        try {
            Auditable a = ((MethodSignature) pjp.getSignature()).getMethod()
                .getAnnotation(Auditable.class);
            saveAuditEntry(a, result);
        } catch (Exception e) {
            // La auditoria es accesoria. Se registra el motivo para diagnostico sin romper el flujo.
            log.warn("Audit log entry could not be saved", e);
        }
        return result;
    }

    /**
     * Este metodo construye la entidad AuditLog y la persiste en una transaccion
     * propia que es independiente de la del metodo auditado. Aislar la auditoria
     * evita que un error en este insert haga rollback de la operacion principal.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveAuditEntry(Auditable annotation, Object result) {
        AuditLog entry = new AuditLog();
        entry.setAccountId(TenantContext.get());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal p) {
            entry.setUserId(p.userId());
        }
        entry.setEntityType(annotation.entityType());
        entry.setAction(annotation.action());
        entry.setEntityId(extractId(result));
        repo.save(entry);
    }

    /**
     * Este metodo intenta obtener el identificador del objeto retornado para
     * persistirlo en la fila de auditoria. Es tolerante a tipos: si el objeto
     * no expone un metodo id ni un metodo getId compatible, devuelve null.
     */
    private Long extractId(Object o) {
        if (o == null) return null;
        return tryInvokeLongAccessor(o, "id")
            .or(() -> tryInvokeLongAccessor(o, "getId"))
            .orElse(null);
    }

    /** Este metodo invoca por reflexion un getter sin argumentos y devuelve el Long si aplica. */
    private java.util.Optional<Long> tryInvokeLongAccessor(Object o, String methodName) {
        try {
            var m = o.getClass().getMethod(methodName);
            Object v = m.invoke(o);
            return v instanceof Long l ? java.util.Optional.of(l) : java.util.Optional.empty();
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }
}
