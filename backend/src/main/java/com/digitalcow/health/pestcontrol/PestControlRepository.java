package com.digitalcow.health.pestcontrol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD de PestControl. */
@Repository
public interface PestControlRepository extends JpaRepository<PestControl, Long>, JpaSpecificationExecutor<PestControl> { }
