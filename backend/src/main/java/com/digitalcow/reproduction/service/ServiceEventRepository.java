package com.digitalcow.reproduction.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD de ServiceEvent. */
@Repository
public interface ServiceEventRepository extends JpaRepository<ServiceEvent, Long>, JpaSpecificationExecutor<ServiceEvent> {

    List<ServiceEvent> findByAnimalIdOrderByServiceDateDesc(Long animalId);
}
