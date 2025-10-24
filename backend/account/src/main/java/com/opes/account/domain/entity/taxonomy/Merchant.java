// com/opes/account/domain/entity/taxonomy/Merchant.java
package com.opes.account.domain.entity.taxonomy;

import com.opes.account.domain.entity.AppUser;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "merchant",
        indexes = @Index(name = "idx_merchant_user_name", columnList = "user_id,name"))
public class Merchant {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // NULL = merchant di sistema
    private AppUser user;

    @Column(nullable = false)
    private String name;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
