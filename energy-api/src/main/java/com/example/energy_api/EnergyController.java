package com.example.energy_api;

import com.example.energy_api.dto.CurrentEnergyDto;
import com.example.energy_api.dto.HistoricalEnergyDto;
import com.example.energy_api.repository.CurrentPercentageEntity;
import com.example.energy_api.repository.CurrentPercentageRepository;
import com.example.energy_api.repository.UsageEntity;
import com.example.energy_api.repository.UsageRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class EnergyController {

    private final UsageRepository usageRepository;
    private final CurrentPercentageRepository currentPercentageRepository;

    public EnergyController(UsageRepository usageRepository,
                            CurrentPercentageRepository currentPercentageRepository) {
        this.usageRepository = usageRepository;
        this.currentPercentageRepository = currentPercentageRepository;
    }

    @GetMapping("/energy/current")
    public CurrentEnergyDto getCurrentEnergy() {
        CurrentPercentageEntity entity = currentPercentageRepository.findFirstByOrderByHourDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return mapCurrentPercentageEntityToDto(entity);
    }

    @GetMapping("/energy/historical")
    public List<HistoricalEnergyDto> getHistoricalEnergy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return usageRepository.findByHourBetweenOrderByHour(start, end)
                .stream()
                .map(this::mapUsageEntityToDto)
                .toList();
    }

    private CurrentEnergyDto mapCurrentPercentageEntityToDto(CurrentPercentageEntity entity) {
        return new CurrentEnergyDto(
                entity.getHour().toString(),
                entity.getCommunityDepleted(),
                entity.getGridPortion()
        );
    }

    private HistoricalEnergyDto mapUsageEntityToDto(UsageEntity entity) {
        return new HistoricalEnergyDto(
                entity.getHour().toString(),
                entity.getCommunityProduced(),
                entity.getCommunityUsed(),
                entity.getGridUsed()
        );
    }
}
