// com/opes/account/repository/insight/InsightEventRepository.java
package com.opes.account.repository.insight;

import com.opes.account.domain.entity.insight.InsightEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InsightEventRepository extends JpaRepository<InsightEvent, UUID> {
    Optional<InsightEvent> findByUserIdAndDate(String userId, LocalDate date);
}
