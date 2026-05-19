package com.digitalcow.audit;

import org.springframework.data.jpa.repository.JpaRepository;

/** Este repositorio consulta y guarda los registros de auditoria en la base de datos. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
