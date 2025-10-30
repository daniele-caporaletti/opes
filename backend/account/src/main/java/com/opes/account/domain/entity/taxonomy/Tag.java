// com/opes/account/domain/entity/taxonomy/Tag.java
package com.opes.account.domain.entity.taxonomy;

import com.opes.account.appuser.domain.entity.AppUser;
import com.opes.account.domain.entity.base.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_tag_user_name", columnNames = {"user_id","name"}))
@Getter @Setter
public class Tag extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private String name;
}
