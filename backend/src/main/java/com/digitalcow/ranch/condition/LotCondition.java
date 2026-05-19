package com.digitalcow.ranch.condition;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;

/**
 * Observacion de una condicion del corral en una fecha dada.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "lot_condition")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class LotCondition extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "observed_at", nullable = false)
    private LocalDate observedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LotConditionKind kind;

    /** Escala opcional 1 a 5. */
    @Column
    private Short severity;

    /** Etiqueta libre cuando kind = CUSTOM, o anotacion adicional. */
    @Column(name = "custom_label", length = 80)
    private String customLabel;

    @Column(length = 400)
    private String notes;

    @Column(name = "recorded_by_user_id")
    private Long recordedByUserId;
}
