package com.example.energy_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

//Spring Data JPA erzeugt aus dem Methodennamen automatisch die Abfrage.
//Dieses Repository liest historische Werte zwischen start und end, sortiert nach hour.
public interface UsageRepository extends JpaRepository<UsageEntity, LocalDateTime> {

    List<UsageEntity> findByHourBetweenOrderByHour(LocalDateTime start, LocalDateTime end);
}
