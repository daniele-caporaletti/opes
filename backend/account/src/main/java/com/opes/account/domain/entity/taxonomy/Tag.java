// com/opes/account/domain/entity/taxonomy/Tag.java
package com.opes.account.domain.entity.taxonomy;

import com.opes.account.domain.entity.AppUser;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_tag_user_name", columnNames = {"user_id", "name"}))
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
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
