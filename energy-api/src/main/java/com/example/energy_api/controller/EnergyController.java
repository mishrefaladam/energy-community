package com.example.energy_api.controller;


import com.example.energy_api.dto.CurrentEnergyDto;
import com.example.energy_api.dto.HistoricalEnergyDto;
import com.example.energy_api.repository.CurrentPercentageEntity;
import com.example.energy_api.repository.CurrentPercentageRepository;
import com.example.energy_api.repository.UsageEntity;
import com.example.energy_api.repository.UsageRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final UsageRepository usageRepository;
    private final CurrentPercentageRepository currentPercentageRepository;

    public EnergyController(UsageRepository usageRepository,
                            CurrentPercentageRepository currentPercentageRepository) {
        this.usageRepository = usageRepository;
        this.currentPercentageRepository = currentPercentageRepository;
    }


    private CurrentEnergyDto mapCurrentPercentageEntityToDto(CurrentPercentageEntity entity) {
        return new CurrentEnergyDto(
                entity.getHour(),
                entity.getCommunityDepleted(),
                entity.getGridPortion()
        );
    }

    private HistoricalEnergyDto mapUsageEntityToDto(UsageEntity entity) {
        return new HistoricalEnergyDto(
                entity.getHour(),
                entity.getCommunityProduced(),
                entity.getCommunityUsed(),
                entity.getGridUsed()
        );
    }


    @GetMapping("/current")
    // e.g. /energy/current
    public CurrentEnergyDto getCurrent() {
        CurrentPercentageEntity entity = this.currentPercentageRepository
                .findFirstByOrderByHourDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return mapCurrentPercentageEntityToDto(entity);
    }

    @GetMapping("/historical")
    // e.g. /energy/historical?start=2025-01-10T00:00:00&end=2025-01-10T23:00:00
    public List<HistoricalEnergyDto> getHistorical(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return this.usageRepository
                .findUsageEntitiesByHourBetweenOrderByHour(start, end)
                .stream()
                .map(entity -> mapUsageEntityToDto(entity))
                .toList();
    }

}
