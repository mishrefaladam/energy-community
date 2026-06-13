package com.example.energy_api.dto;

public class CurrentEnergyDto {

    //DTO für /energy/current: Dieses Objekt wird von Spring automatisch als JSON serialisiert.
    //Es enthält nur die Werte, die die GUI wirklich anzeigen soll.
    private final String hour;
    private final double communityDepleted;
    private final double gridPortion;

    public CurrentEnergyDto(String hour, double communityDepleted, double gridPortion) {
        this.hour = hour;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    //getter methoden um die werte zurückzugeben.(als json)
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
