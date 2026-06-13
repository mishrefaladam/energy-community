package com.example.energy_gui.dto;

import java.time.LocalDateTime;

//Dieses DTO entspricht der JSON-Antwort von /energy/current.
public class CurrentEnergyDto {
    private LocalDateTime hour;
    private double communityDepleted;
    private double gridPortion;

    //Der leere Konstruktor ist wichtig, damit Jackson das Objekt aus JSON erstellen kann.
    public CurrentEnergyDto() {
    }

    //Getter und Setter sind wichtig, damit Jackson die JSON-Felder korrekt setzen und die GUI die Werte lesen kann.
    public LocalDateTime getHour() {
        return hour;
    }

    public void setHour(LocalDateTime hour) {
        this.hour = hour;
    }

    public double getCommunityDepleted() {
        return communityDepleted;
    }

    public void setCommunityDepleted(double communityDepleted) {
        this.communityDepleted = communityDepleted;
    }

    public double getGridPortion() {
        return gridPortion;
    }

    public void setGridPortion(double gridPortion) {
        this.gridPortion = gridPortion;
    }
}
