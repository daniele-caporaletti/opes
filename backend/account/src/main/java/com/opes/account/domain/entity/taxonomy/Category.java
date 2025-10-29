// com/opes/account/domain/entity/taxonomy/Category.java
package com.opes.account.domain.entity.taxonomy;

import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.enums.CategoryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "category",
        indexes = @Index(name = "idx_category_user_type_name", columnList = "user_id,type,name")
)
@Getter @Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // NULL = categoria di sistema; valorizzato = categoria personalizzata utente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent; // opzionale: per gerarchie
}
