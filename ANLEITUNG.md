# Anleitung – Energy Community Projekt einrichten und starten

Diese Anleitung führt euch Schritt für Schritt durch alles, was nötig ist um das Projekt auf eurem Rechner zum Laufen zu bekommen. Sie ist absichtlich ausführlich – auch wenn ihr Docker schon installiert habt, lest die ersten Schritte zur Sicherheit nochmal durch.

---

## 0. Voraussetzungen

Diese Tools müssen installiert sein:

* **Java JDK 21** (oder neuer). Check: `java -version` im Terminal
* **Maven** ist optional – jedes Projekt hat einen `mvnw` Wrapper. Wenn ihr `mvn` global habt, könnt ihr das auch nehmen
* **IntelliJ IDEA** (Community oder Ultimate)
* **Docker Desktop** (Windows oder Mac). Muss laufen, bevor ihr loslegt – ihr seht das Whale-Icon in der Taskleiste

Falls Docker Desktop noch nicht läuft: starten und warten bis das Whale-Icon nicht mehr animiert ist.

---

## 1. Projekt entpacken

Das Zip enthält den Ordner `energy-community/`. Entpackt es irgendwohin, zum Beispiel `C:\Users\<name>\IdeaProjects\energy-community\`. Wichtig: **nicht in einen Pfad mit Sonderzeichen oder Leerzeichen**, das verträgt Maven manchmal nicht gut.

Die Struktur sieht so aus:

```
energy-community/
├── docker/
│   └── docker-compose.yml
├── energy-api/
├── energy-gui/
├── energy-producer/
├── energy-user/
├── usage-service/
├── percentage-service/
├── README.md
└── ANLEITUNG.md  (diese Datei)
```

---

## 2. Docker starten (PostgreSQL + RabbitMQ)

Im Projektordner liegt `docker/docker-compose.yml`. Diese Datei startet zwei Container:

* **PostgreSQL** auf Port 5432 mit der Datenbank `energydb`
* **RabbitMQ** auf Port 5672, plus eine Management-UI auf Port 15672

### 2.1. Container hochfahren

Terminal im Projekt-Root öffnen (also dort wo die `README.md` liegt), dann:

```bash
cd docker
docker compose up -d
```

Das `-d` heißt "detached" – die Container laufen im Hintergrund. Beim ersten Mal lädt Docker die Images runter, das dauert eine Minute.

### 2.2. Checken, dass die Container laufen

```bash
docker ps
```

Ihr solltet zwei Zeilen sehen:

```
CONTAINER ID   IMAGE                             ...   NAMES
xxxxxxxxxxxx   postgres:alpine                   ...   energy-postgres
xxxxxxxxxxxx   rabbitmq:management-alpine        ...   energy-rabbitmq
```

Wenn beide Container laufen: super, weiter zum nächsten Schritt.

### 2.3. RabbitMQ Management UI öffnen (zum Checken)

Im Browser: <http://localhost:15672>

Login:
* User: `guest`
* Password: `guest`

In der Management-UI seht ihr später die Queues (`energy_messages`, `usage_updates`) und wie viele Messages durchgehen. Sehr nützlich beim Debuggen und beim Code Review – ihr könnt dem Prof live zeigen, dass Messages fließen.

### 2.4. Container stoppen (später, wenn ihr fertig seid)

```bash
cd docker
docker compose down
```

Wenn ihr die Daten in der Datenbank löschen wollt (z.B. um sauber neu zu starten):

```bash
docker compose down -v
```

Das `-v` löscht auch das persistente Volume.

---

## 3. Projekte in IntelliJ öffnen

Hier gibt es **zwei Varianten**. Variante A ist einfacher und reicht.

### Variante A: Alle Projekte in einem Workspace (empfohlen)

1. In IntelliJ: **File → Open**
2. Den Ordner `energy-community/` auswählen und öffnen
3. IntelliJ fragt eventuell: "This directory contains multiple Maven projects. Import them?" → **Ja**
4. IntelliJ erkennt nun die 6 `pom.xml` Dateien und importiert alle als Module

Falls IntelliJ nicht von alleine fragt:
* Rechtsklick auf jeden `pom.xml` → "Add as Maven Project"
* Oder: File → Project Structure → Modules → "+" → "Import Module" und einzeln durchgehen

Es kann ein paar Minuten dauern, bis IntelliJ alle Dependencies runtergeladen hat. Unten im Status-Balken seht ihr "Resolving Maven dependencies". Wartet, bis das fertig ist.

### Variante B: Jedes Projekt als eigenes Fenster

Falls Variante A nicht klappt: für jedes der sechs Unterordner (`energy-api/`, `energy-gui/`, ...) einzeln `File → Open` machen. Dann habt ihr 6 IntelliJ-Fenster auf einmal offen. Funktional kein Unterschied, nur unübersichtlicher.

---

## 4. Database Tool Window konfigurieren (optional, aber sehr nützlich)

In IntelliJ Ultimate könnt ihr direkt in der IDE auf die PostgreSQL-Datenbank zugreifen. In der Community Edition geht das nicht, dann nehmt ihr extern z.B. **DBeaver** oder **pgAdmin**.

### IntelliJ Ultimate

1. Rechte Seitenleiste: **Database** Tab (falls nicht sichtbar: View → Tool Windows → Database)
2. "+" → Data Source → PostgreSQL
3. Eintragen:
   * **Host**: `localhost`
   * **Port**: `5432`
   * **Database**: `energydb`
   * **User**: `disysuser`
   * **Password**: `disyspw`
4. Eventuell sagt IntelliJ "Download missing driver files" → auf den Link klicken, abwarten
5. "Test Connection" → muss grün werden
6. OK

Jetzt seht ihr links die Datenbank `energydb` mit den Schemas. Nachdem `energy-api` einmal gestartet ist, sind dort die Tabellen `energy_usage` und `current_percentage`.

### Mit DBeaver (Community Edition)

1. DBeaver runterladen und installieren
2. Neue Verbindung → PostgreSQL
3. Gleiche Werte wie oben
4. "Test Connection" → grün → Finish

---

## 5. Reihenfolge zum Starten der Apps

**Wichtige Reihenfolge:**

1. Docker Services starten:
   `cd docker && docker compose up -d`
2. REST API starten:
   `cd energy-api && ./mvnw spring-boot:run`
3. Usage Service starten:
   `cd usage-service && ./mvnw spring-boot:run`
4. Percentage Service starten:
   `cd percentage-service && ./mvnw spring-boot:run`
5. Energy Producer starten:
   `cd energy-producer && ./mvnw spring-boot:run`
6. Energy User starten:
   `cd energy-user && ./mvnw spring-boot:run`
7. JavaFX GUI starten:
   `cd energy-gui && mvn clean javafx:run`

In IntelliJ geht das so für jede App:

### 5.1. Spring Boot Apps starten (energy-api, usage-service, percentage-service, energy-producer, energy-user)

Für jede dieser Apps:

1. Im Projekt-Explorer den Ordner aufklappen
2. Den Pfad bis zur Application-Klasse durchgehen, z.B.:
   * `energy-api/src/main/java/com/example/energy_api/EnergyApiApplication.java`
   * `usage-service/src/main/java/com/example/usage_service/UsageServiceApplication.java`
   * etc.
3. Rechtsklick auf die Application-Klasse → **Run 'XxxApplication.main()'**
4. Im Run-Tab unten erscheinen die Logs

Beim ersten Mal lädt Maven die Dependencies runter, das kann etwas dauern.

### 5.2. JavaFX GUI starten (energy-gui)

Bei JavaFX nicht über die Application-Klasse starten, sondern über Maven:

1. Im rechten Maven-Tab: `energy-gui` aufklappen → `Plugins` → `javafx` → Doppelklick auf `javafx:run`
2. Oder im Terminal: `cd energy-gui` und dann `mvn clean javafx:run`

Die GUI öffnet sich nach ein paar Sekunden.

---

## 6. Was wann passiert

Nachdem alle Apps gestartet sind:

* **energy-producer** sendet alle 1–5 Sekunden eine Message mit `type: PRODUCER` an die Queue `energy_messages`. Der kWh-Wert hängt von der aktuellen Bewölkung in Wien ab (Sonne = mehr Produktion).
* **energy-user** sendet alle 1–5 Sekunden eine Message mit `type: USER` an die selbe Queue. Mehr kWh in den Peak-Stunden (~8 Uhr und ~19 Uhr).
* **usage-service** hört auf die Queue, akkumuliert die Werte pro Stunde in die Tabelle `energy_usage`. Wenn ein User mehr will als die Community produziert hat, wird der Rest vom Grid genommen.
* **usage-service** sendet nach jedem Update eine Message an die Queue `usage_updates`.
* **percentage-service** hört auf `usage_updates` und schreibt die berechneten Prozentwerte in die Tabelle `current_percentage`.
* In der **GUI** könnt ihr mit "refresh" die aktuellen Prozentwerte abrufen und mit "show data" historische Stunden anschauen.

Ihr könnt das live verfolgen:

* In der **RabbitMQ Management UI** (<http://localhost:15672>) → Queues → seht ihr die Message-Raten
* Im **Database Tool** in IntelliJ oder in DBeaver → Tabellen öffnen → seht ihr die Werte wachsen
* In den Run-Konsolen der Services → seht ihr "Received message from queue" Logs

---

## 7. Troubleshooting

### "Connection refused" beim Start einer App

* Docker läuft nicht oder die Container sind nicht hochgefahren → siehe Schritt 2
* Falscher Port → check `docker ps`, stelle sicher dass 5432 und 5672 frei sind

### "FATAL: database 'energydb' does not exist"

Datenbank wurde nicht erstellt. Das passiert wenn ihr Docker schon mal mit anderen Settings gestartet hattet. Fix:

```bash
cd docker
docker compose down -v
docker compose up -d
```

Das `-v` löscht das alte Volume, der nächste Start erstellt eine frische Datenbank.

### "Flyway migration failed"

Wahrscheinlich liegen schon Tabellen in der Datenbank. Selbe Lösung wie oben: `docker compose down -v` und neu starten.

### Die GUI sagt "Could not reach the API"

`energy-api` läuft nicht oder noch nicht. Wartet bis im Run-Tab steht: `Started EnergyApiApplication in X seconds`.

### Die GUI öffnet sich nicht, statt dessen kommt ein Modul-Fehler

`./mvnw javafx:run` nutzen statt über die Application-Klasse starten. JavaFX braucht das Maven-Plugin, sonst stimmen die Modul-Pfade nicht.

### Maven lädt nichts runter / "Could not resolve dependencies"

* Internet checken
* In IntelliJ: rechts im Maven-Tab oben auf "Reload All Maven Projects" klicken (das Refresh-Icon)

### Eine App startet aber keine Messages kommen an

* Schaut in die RabbitMQ Management UI: <http://localhost:15672>
* Tab "Queues": ist die Queue `energy_messages` da? Sollte automatisch beim Start des Services angelegt werden
* Tab "Connections": sind beide Apps verbunden?

### Tests schlagen fehl mit "Connection refused"

Die Spring Boot Tests versuchen die DB-Connection herzustellen. Vor dem Testen: Docker muss laufen. Oder Tests beim Build überspringen mit `./mvnw clean package -DskipTests`.

---

## 8. Build aller Projekte

Falls ihr alles bauen wollt (z.B. um Jar-Files zu erstellen):

```bash
cd energy-api && ./mvnw clean package -DskipTests && cd ..
cd usage-service && ./mvnw clean package -DskipTests && cd ..
cd percentage-service && ./mvnw clean package -DskipTests && cd ..
cd energy-producer && ./mvnw clean package -DskipTests && cd ..
cd energy-user && ./mvnw clean package -DskipTests && cd ..
cd energy-gui && ./mvnw clean package -DskipTests && cd ..
```

Auf Windows die `.cmd`-Variante: `mvnw.cmd clean package -DskipTests`.

---

## 9. Tipps für den Code Review

Der Prof bewertet das Projekt mit einem Code Review. Erfahrungsgemäß fragt er:

* "Was passiert, wenn ein USER mehr Energie braucht als produziert wurde?"
  → Antwort: Der Usage Service zieht zuerst aus dem Community-Pool (`community_used`), und der Rest geht über das Grid (`grid_used`). Diese Logik ist in `UsageProcessor.readFromEnergyMessages` zu sehen.

* "Wie kommt eine Message vom Producer bis in die GUI?"
  → Producer sendet JSON → RabbitMQ-Queue `energy_messages` → Usage Service liest, parsed, aggregiert in `energy_usage` Tabelle, sendet Update → Queue `usage_updates` → Percentage Service berechnet Prozente → speichert in `current_percentage` → GUI ruft `/energy/current` → REST API liest aus DB → JSON zurück an GUI.

* "Warum habt ihr Flyway benutzt?"
  → Datenbank-Schema-Migrations versioniert verwalten – das macht der Prof in Class 10 genauso. `energy-api` besitzt die Migrations, alle anderen Services nutzen `spring.jpa.hibernate.ddl-auto=none` und greifen einfach auf existierende Tabellen zu.

* "Was passiert, wenn der Pool noch leer ist (community_produced = 0) und ein User Strom braucht?"
  → Dann ist `available = 0`, also kommt alles vom Grid (`grid_used` += kwh).

* "Wo deklariert ihr die Queues?"
  → In den jeweiligen `XxxApplication.java` als `@Bean`. Genauso wie in Lecture 11 in `Disysserver26Application`.

Bereitet euch zu dritt darauf vor, dass **jeder** etwas erklären kann – das gibt einem die volle Punktzahl.
