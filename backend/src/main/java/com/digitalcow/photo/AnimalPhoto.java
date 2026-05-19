package com.digitalcow.photo;

import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.Instant;

/** Foto del animal, almacenada en Cloudinary. */
@Entity
@Table(name = "animal_photo")
@EntityListeners(TenantAwareEntityListener.class)
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter @Setter @NoArgsConstructor
public class AnimalPhoto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "cloudinary_public_id", nullable = false, length = 200)
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_url", nullable = false, length = 500)
    private String cloudinaryUrl;

    private Integer width;
    private Integer height;
    private Integer bytes;

    @Column(name = "taken_at")
    private Instant takenAt;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private Long uploadedByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
