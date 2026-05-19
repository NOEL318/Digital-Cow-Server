package com.digitalcow.feeding.feeditem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD de FeedItem. */
@Repository
public interface FeedItemRepository extends JpaRepository<FeedItem, Long>, JpaSpecificationExecutor<FeedItem> {
}
