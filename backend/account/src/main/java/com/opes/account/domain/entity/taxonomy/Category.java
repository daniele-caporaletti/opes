// com/opes/account/domain/entity/taxonomy/Category.java
package com.opes.account.domain.entity.taxonomy;

import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.enums.CategoryType;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "category",
        indexes = @Index(name = "idx_cat_user_type_name", columnList = "user_id,type,name"))
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // NULL = categoria di sistema
    private AppUser user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }
}
