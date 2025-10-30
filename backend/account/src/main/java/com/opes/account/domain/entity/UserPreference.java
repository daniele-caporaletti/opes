// com/opes/account/domain/entity/UserPreference.java
package com.opes.account.domain.entity;

import com.opes.account.appuser.domain.entity.AppUser;
import com.opes.account.domain.entity.base.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_preference",
        uniqueConstraints = @UniqueConstraint(name = "uk_userpref_user_key", columnNames = {"user_id","pref_key"}),
        indexes = @Index(name = "idx_userpref_user", columnList = "user_id"))
@Getter @Setter
public class UserPreference extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "pref_key", nullable = false, length = 64)
    private String key;

    @Column(name = "pref_value", nullable = false, length = 64)
    private String value;
}
