package com.example.energy_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

// types are the entity type (table) and the type of the primary key of the table
public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentageEntity, LocalDateTime> {

    Optional<CurrentPercentageEntity> findFirstByOrderByHourDesc();
}
