// com/opes/account/repository/taxonomy/MerchantRepository.java
package com.opes.account.repository.taxonomy;

import com.opes.account.domain.entity.taxonomy.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    /**
     * Ritorna (id, name) per un set di merchantId.
     * Utile per costruire una mappa id->label nelle classifiche Top Merchant.
     */
    @Query("""
        select m.id, m.name
        from Merchant m
        where m.id in :ids
    """)
    List<Object[]> findIdAndNameByIds(Set<UUID> ids);
}
