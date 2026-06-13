package com.example.percentage_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

//Dieses Repository speichert und löscht Einträge der Tabelle current_percentage.
public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentageEntity, LocalDateTime> {
}
