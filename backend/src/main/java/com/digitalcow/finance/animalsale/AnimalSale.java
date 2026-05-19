package com.digitalcow.finance.animalsale;

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

/** Venta de un animal del inventario. UQ(account_id, animal_id) impide vender 2 veces. */
@Entity
@Table(name = "animal_sale")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class AnimalSale extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "sold_at", nullable = false)
    private LocalDate soldAt;

    @Column(name = "live_weight_kg", precision = 7, scale = 2)
    private BigDecimal liveWeightKg;

    @Column(name = "price_per_kg", precision = 10, scale = 4)
    private BigDecimal pricePerKg;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 3)
    private String currency;

    @Column(length = 160)
    private String buyer;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}
