package com.digitalcow.reproduction.bull;

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

/**
 * Toro del catalogo reproductivo. Puede ser propio (OWN) si es un animal de la cuenta
 * o externo (EXTERNAL) como referencia para inseminacion artificial.
 * Multi-tenant via filtro accountFilter.
 */
@Entity
@Table(name = "bull")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class Bull extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "internal_code", nullable = false, length = 60)
    private String internalCode;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "breed_id")
    private Long breedId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BullSource source;

    @Column(name = "animal_id")
    private Long animalId;

    @Column(name = "registry_number", length = 80)
    private String registryNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
