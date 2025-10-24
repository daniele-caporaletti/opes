// com/opes/account/repository/taxonomy/CategoryRepository.java
package com.opes.account.repository.taxonomy;

import com.opes.account.domain.entity.taxonomy.Category;
import com.opes.account.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserIdIsNullAndType(CategoryType type); // di sistema
    List<Category> findByUserIdAndType(String userId, CategoryType type); // personalizzate
}
