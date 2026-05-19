package com.digitalcow.production.bulktank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD de BulkTankDelivery. */
@Repository
public interface BulkTankDeliveryRepository extends JpaRepository<BulkTankDelivery, Long>, JpaSpecificationExecutor<BulkTankDelivery> {
}
