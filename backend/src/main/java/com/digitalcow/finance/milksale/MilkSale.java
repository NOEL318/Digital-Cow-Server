package com.digitalcow.finance.milksale;

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

/** Venta de leche del tenant (granel o directa). */
@Entity
@Table(name = "milk_sale")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter
@Setter
public class MilkSale extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(name = "total_liters", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalLiters;

    @Column(name = "price_per_liter", nullable = false, precision = 10, scale = 4)
    private BigDecimal pricePerLiter;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 3)
    private String currency;

    @Column(length = 160)
    private String buyer;

    @Column(name = "bulk_tank_delivery_id")
    private Long bulkTankDeliveryId;

    @Column(name = "ranch_id")
    private Long ranchId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}
