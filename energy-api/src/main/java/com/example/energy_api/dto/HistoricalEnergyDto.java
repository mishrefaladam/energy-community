package com.example.energy_api.dto;

public class HistoricalEnergyDto {

    private final String hour;
    private final double communityProduced;
    private final double communityUsed;
    private final double gridUsed;

    public HistoricalEnergyDto(String hour, double communityProduced, double communityUsed, double gridUsed) {
        this.hour = hour;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
    }

    public String getHour() {
        return hour;
    }

    public double getCommunityProduced() {
        return communityProduced;
    }

    public double getCommunityUsed() {
        return communityUsed;
    }

    public double getGridUsed() {
        return gridUsed;
    }
}
