// com/opes/account/repository/taxonomy/MerchantRepository.java
package com.opes.account.repository.taxonomy;

import com.opes.account.domain.entity.taxonomy.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    List<Merchant> findByUserIdAndNameContainingIgnoreCase(String userId, String q);
}
