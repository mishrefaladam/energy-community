package com.example.usage_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

// types are the entity type (table) and the type of the primary key of the table
public interface UsageRepository extends JpaRepository<UsageEntity, LocalDateTime> {
}
