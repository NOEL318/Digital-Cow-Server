package com.digitalcow.breed;

import jakarta.persistence.*;
import lombok.*;

/** Raza bovina del catalogo global (no multi-tenant). */
@Entity
@Table(name = "breed")
@Getter @Setter @NoArgsConstructor
public class Breed {

    public enum Species { BOVINE }
    public enum Category { DAIRY, BEEF, DUAL }
    public enum Bos { TAURUS, INDICUS, CROSS }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String code;

    @Column(name = "name_es", nullable = false, length = 120)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 120)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Species species;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Bos bos;
}
