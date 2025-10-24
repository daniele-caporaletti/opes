// com/opes/account/repository/taxonomy/TagRepository.java
package com.opes.account.repository.taxonomy;

import com.opes.account.domain.entity.taxonomy.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByUserId(String userId);
}
