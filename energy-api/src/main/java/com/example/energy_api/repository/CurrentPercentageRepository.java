package com.example.energy_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

//Dieses Repository holt den neuesten aktuellen Prozentwert.
public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentageEntity, LocalDateTime> {

    Optional<CurrentPercentageEntity> findFirstByOrderByHourDesc();
}
