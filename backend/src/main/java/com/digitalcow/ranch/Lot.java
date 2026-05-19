package com.digitalcow.ranch;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;

/**
 * Lote (potrero / corral) dentro de un rancho. Soporta poligono
 * geografico en formato JSON: [[lat,lng],[lat,lng],...] y centro
 * lat/lng para mostrar marker cuando aun no hay poligono.
 */
@Entity
@Table(name = "lot")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter @Setter @NoArgsConstructor
public class Lot extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "ranch_id", nullable = false)
    private Long ranchId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "area_hectares", precision = 10, scale = 2)
    private BigDecimal areaHectares;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Lista de [lat,lng] como JSON. Null = sin poligono dibujado. */
    @Column(columnDefinition = "json")
    private String polygon;

    @Column(name = "center_lat", precision = 9, scale = 6)
    private BigDecimal centerLat;

    @Column(name = "center_lng", precision = 9, scale = 6)
    private BigDecimal centerLng;
}
