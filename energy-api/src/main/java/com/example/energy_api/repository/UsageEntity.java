package com.example.energy_api.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

//Diese Entity bildet die Tabelle energy_usage in Java ab.
//@Entity(name = "energy_usage") verbindet die Klasse mit dieser Tabelle.
@Entity(name = "energy_usage")
public class UsageEntity {

    //@Id markiert den Primary Key.
    @Id
    @Column(name = "hour")
    private LocalDateTime hour;

    //@Column(...) verbindet Java-Felder mit DB-Spalten.
    @Column(name = "community_produced")
    private double communityProduced;

    @Column(name = "community_used")
    private double communityUsed;

    @Column(name = "grid_used")
    private double gridUsed;

    public LocalDateTime getHour() {
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
