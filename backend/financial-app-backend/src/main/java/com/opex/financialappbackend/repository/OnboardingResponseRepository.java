package com.opex.financialappbackend.repository;

import com.opex.financialappbackend.domain.OnboardingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnboardingResponseRepository extends JpaRepository<OnboardingResponse, Long> {
    List<OnboardingResponse> findByUserId(String userId);
}