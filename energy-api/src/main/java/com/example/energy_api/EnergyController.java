package com.example.energy_api;

import com.example.energy_api.dto.CurrentEnergyDto;
import com.example.energy_api.dto.HistoricalEnergyDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EnergyController {

    @GetMapping("/energy/current")
    public CurrentEnergyDto getCurrentEnergy() {
        return new CurrentEnergyDto(
                "2025-01-10T14:00:00",
                100.0,
                5.63
        );
    }

    @GetMapping("/energy/historical")
    public List<HistoricalEnergyDto> getHistoricalEnergy(
            @RequestParam String start,
            @RequestParam String end
    ) {
        return List.of(
                new HistoricalEnergyDto(
                        "2025-01-10T13:00:00",
                        15.015,
                        14.033,
                        2.049
                ),
                new HistoricalEnergyDto(
                        "2025-01-10T14:00:00",
                        18.05,
                        18.05,
                        1.076
                )
        );
    }
}
