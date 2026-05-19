package com.digitalcow.health.vetvisit;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Visita veterinaria. Agrupa eventos sanitarios del mismo dia y rancho.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "vet_visit")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class VetVisit extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "ranch_id", nullable = false)
    private Long ranchId;

    @Column(name = "visited_at", nullable = false)
    private LocalDate visitedAt;

    @Column(name = "vet_name", nullable = false, length = 160)
    private String vetName;

    @Column(name = "vet_contact", length = 160)
    private String vetContact;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}
