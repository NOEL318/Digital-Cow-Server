package com.digitalcow.catalog.pest;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Catalogo global de plagas y parasitos. Sin account_id.
 */
@Entity
@Table(name = "pest")
@Getter
@Setter
public class Pest extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "name_es", nullable = false, length = 160)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 160)
    private String nameEn;

    @Column(name = "scientific_name", length = 160)
    private String scientificName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private PestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private PestRegion region = PestRegion.ANY;

    @Column(length = 400)
    private String notes;
}
