package com.example.energy_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentageEntity, LocalDateTime> {

    Optional<CurrentPercentageEntity> findFirstByOrderByHourDesc();
}
