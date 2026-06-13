package com.example.usage_service.dto;

public class EnergyMessageDto {
    //Dieses DTO entspricht dem JSON-Format in der Queue energy_messages.
    //Producer und User senden dasselbe Format, unterscheiden sich aber über type.
    private String type;          // PRODUCER or USER
    private String association;   // COMMUNITY
    private double kwh;
    private String datetime;      // e.g. 2025-01-10T14:33:00

    //Der leere Konstruktor ist wichtig für Jackson/ObjectMapper.
    //Er kann damit ein leeres DTO-Objekt erzeugen und anschließend die Werte aus dem JSON über Setter eintragen.
    public EnergyMessageDto() {
    }

    public EnergyMessageDto(String type, String association, double kwh, String datetime) {
        this.type = type;
        this.association = association;
        this.kwh = kwh;
        this.datetime = datetime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public double getKwh() {
        return kwh;
    }

    public void setKwh(double kwh) {
        this.kwh = kwh;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
