package com.example.energy_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class EnergyController {

    @GetMapping("/energy/current")
    public Map<String, Object> getCurrentEnergy() {
        return Map.of(
                "hour", "2025-01-10T14:00:00",
                "communityDepleted", 100.0,
                "gridPortion", 5.63
        );
    }

    @GetMapping("/energy/historical")
    public List<Map<String, Object>> getHistoricalEnergy(
            @RequestParam String start,
            @RequestParam String end
    ) {
        return List.of(
                Map.of(
                        "hour", "2025-01-10T13:00:00",
                        "communityProduced", 15.015,
                        "communityUsed", 14.033,
                        "gridUsed", 2.049
                ),
                Map.of(
                        "hour", "2025-01-10T14:00:00",
                        "communityProduced", 18.05,
                        "communityUsed", 18.05,
                        "gridUsed", 1.076
                )
        );
    }
}

