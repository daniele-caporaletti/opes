// com/opes/account/repository/taxonomy/CategoryRepository.java
package com.opes.account.repository.taxonomy;

import com.opes.account.domain.entity.taxonomy.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, java.util.UUID> {

    /**
     * Ritorna (id, name) per un set di categoryId.
     * Utile per costruire rapidamente una mappa id->label nelle classifiche.
     */
    @Query("""
        select c.id, c.name
        from Category c
        where c.id in :ids
    """)
    List<Object[]> findIdAndNameByIds(Set<UUID> ids);
}
