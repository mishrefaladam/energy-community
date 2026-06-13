/*Diese Migration erstellt die zwei zentralen Tabellen.*/
/*Tabelle 1: historische Produktions-/Verbrauchsdaten pro Stunde.*/
CREATE TABLE IF NOT EXISTS energy_usage (
    hour TIMESTAMP PRIMARY KEY,
    /*Fließkommazahl, wert nicht 0, standartwert 0*/
    community_produced DOUBLE PRECISION NOT NULL DEFAULT 0,
    community_used DOUBLE PRECISION NOT NULL DEFAULT 0,
    grid_used DOUBLE PRECISION NOT NULL DEFAULT 0
);

/*Tabelle 2: aktueller Prozentwert, den die GUI über /energy/current anzeigt.*/
CREATE TABLE IF NOT EXISTS current_percentage (
    hour TIMESTAMP PRIMARY KEY,
    community_depleted DOUBLE PRECISION NOT NULL DEFAULT 0,
    grid_portion DOUBLE PRECISION NOT NULL DEFAULT 0
);
