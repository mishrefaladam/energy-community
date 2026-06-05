package com.example.energy_api.dto;

public class CurrentEnergyDto {

    private final String hour;
    private final double communityDepleted;
    private final double gridPortion;

    public CurrentEnergyDto(String hour, double communityDepleted, double gridPortion) {
        this.hour = hour;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    public String getHour() {
        return hour;
    }

    public double getCommunityDepleted() {
        return communityDepleted;
    }

    public double getGridPortion() {
        return gridPortion;
    }
}
