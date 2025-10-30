// com/opes/account/repository/UserPreferenceRepository.java
package com.opes.account.repository;

import com.opes.account.domain.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {
    Optional<UserPreference> findByUser_IdAndKey(String userId, String key);
}
