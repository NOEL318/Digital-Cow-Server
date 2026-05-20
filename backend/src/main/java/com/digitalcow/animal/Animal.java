package com.digitalcow.animal;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Animal del inventario del tenant. */
@Entity
@Table(name = "animal")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter @Setter @NoArgsConstructor
public class Animal extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "ranch_id", nullable = false)
    private Long ranchId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "internal_tag", nullable = false, length = 40)
    private String internalTag;

    @Column(name = "official_tag", length = 60)
    private String officialTag;

    @Column(length = 40)
    private String rfid;

    @Column(length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Sex sex;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_date_estimated", nullable = false)
    private boolean birthDateEstimated;

    @Column(name = "breed_id", nullable = false)
    private Long breedId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Purpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AnimalStatus status = AnimalStatus.ACTIVE;

    @Column(name = "cover_photo_id")
    private Long coverPhotoId;

    @Column(name = "sire_id")
    private Long sireId;

    @Column(name = "external_sire_name", length = 160)
    private String externalSireName;

    @Column(name = "dam_id")
    private Long damId;

    @Column(name = "birth_weight_kg", precision = 5, scale = 2)
    private BigDecimal birthWeightKg;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    /**
     * Token publico opcional. Se genera bajo demanda cuando el dueno
     * pide compartir el animal. Quien tenga el token accede a una vista
     * de solo lectura sin necesidad de autenticarse.
     */
    @Column(name = "share_token", length = 64)
    private String shareToken;
}
