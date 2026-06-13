package com.example.usage_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

//Die generischen Typen sind Entity-Klasse und Primary-Key-Typ.
//UsageEntity bildet die Tabelle ab; LocalDateTime ist der Typ des Primary Keys hour.
public interface UsageRepository extends JpaRepository<UsageEntity, LocalDateTime> {
}
