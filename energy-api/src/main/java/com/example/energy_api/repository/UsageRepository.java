package com.example.energy_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageRepository extends JpaRepository<UsageEntity, LocalDateTime> {

    List<UsageEntity> findByHourBetweenOrderByHour(LocalDateTime start, LocalDateTime end);
}
