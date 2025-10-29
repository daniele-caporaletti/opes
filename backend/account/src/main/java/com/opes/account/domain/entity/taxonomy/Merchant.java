// com/opes/account/domain/entity/taxonomy/Merchant.java
package com.opes.account.domain.entity.taxonomy;

import com.opes.account.domain.entity.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "merchant",
        indexes = @Index(name = "idx_merchant_user_name", columnList = "user_id,name")
)
@Getter @Setter
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // NULL = merchant di sistema; valorizzato = merchant personalizzato dall’utente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private String name;
}
