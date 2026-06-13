package com.example.energy_api.dto;

public class HistoricalEnergyDto {

    //DTO für /energy/historical: Eine Liste dieser Objekte wird an die GUI gesendet.
    //Die Entity bleibt intern; nach außen geht nur diese vereinfachte JSON-Struktur.
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

    //getter methoden um die werte zurückzugeben.(als json)
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
