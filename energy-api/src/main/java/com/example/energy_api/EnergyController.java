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

//Diese Klasse ist die Schnittstelle zwischen GUI und Datenbank.
//Sie stellt REST-Endpunkte bereit und gibt keine Entity-Objekte direkt zurück, sondern DTOs.
@RestController
public class EnergyController {

    //Die REST API bekommt Zugriff auf die Datenbank über Spring-Data-Repositories.
    //UsageRepository liest historische Stundenwerte; CurrentPercentageRepository liest den aktuellen Prozentwert.
    private final UsageRepository usageRepository;
    private final CurrentPercentageRepository currentPercentageRepository;

    // die repos werden hier bereitgestellt.
    public EnergyController(UsageRepository usageRepository,
                            CurrentPercentageRepository currentPercentageRepository) {
        this.usageRepository = usageRepository;
        this.currentPercentageRepository = currentPercentageRepository;
    }

    //GET /energy/current liefert den aktuellen Zustand für die GUI.
    //Wenn noch kein Prozentwert vorhanden ist, antwortet die API mit 404 NOT_FOUND.
    @GetMapping("/energy/current")
    public CurrentEnergyDto getCurrentEnergy() {
        CurrentPercentageEntity entity = currentPercentageRepository.findFirstByOrderByHourDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return mapCurrentPercentageEntityToDto(entity);
    }
    //GET /energy/historical liest Daten aus energy_usage zwischen Start- und Endzeit.
    //Die Sortierung passiert im Repository-Methodennamen: OrderByHour.
    @GetMapping("/energy/historical")
    public List<HistoricalEnergyDto> getHistoricalEnergy(
            //@RequestParam bedeutet: start und end kommen als Query-Parameter aus der URL.
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return usageRepository.findByHourBetweenOrderByHour(start, end)
                .stream()
                .map(this::mapUsageEntityToDto)
                .toList();
    }

    //Mapping Entity -> DTO: Die API kontrolliert damit, welche Felder als JSON nach außen gehen.
    private CurrentEnergyDto mapCurrentPercentageEntityToDto(CurrentPercentageEntity entity) {
        return new CurrentEnergyDto(
                entity.getHour().toString(),
                entity.getCommunityDepleted(),
                entity.getGridPortion()
        );
    }

    //Auch historische DB-Zeilen werden zuerst in ein DTO umgewandelt.
    private HistoricalEnergyDto mapUsageEntityToDto(UsageEntity entity) {
        return new HistoricalEnergyDto(
                entity.getHour().toString(),
                entity.getCommunityProduced(),
                entity.getCommunityUsed(),
                entity.getGridUsed()
        );
    }
}
