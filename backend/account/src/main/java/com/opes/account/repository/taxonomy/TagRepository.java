// com/opes/account/repository/taxonomy/TagRepository.java
package com.opes.account.repository.taxonomy;

import com.opes.account.domain.entity.taxonomy.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Ritorna (id, name) per un set di tagId.
     * Utile per costruire una mappa id->label nelle classifiche Top Tag.
     */
    @Query("""
        select t.id, t.name
        from Tag t
        where t.id in :ids
    """)
    List<Object[]> findIdAndNameByIds(Set<UUID> ids);
}
