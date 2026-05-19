package com.digitalcow.feeding.record;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD de FeedingRecord. */
@Repository
public interface FeedingRecordRepository extends JpaRepository<FeedingRecord, Long>, JpaSpecificationExecutor<FeedingRecord> {
}
