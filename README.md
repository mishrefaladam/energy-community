# Energy Community

Distributed Systems – Semester Project (BWI-BB-4, SS2026).

A small distributed system that simulates an energy community. Two senders
push production and usage messages into RabbitMQ, two services consume
those messages and update a PostgreSQL database, a Spring Boot REST API
exposes the data and a JavaFX desktop GUI shows it.

## Components

| Folder                   | Description |
| ------------------------ | ----------- |
| `docker/`                | docker-compose.yml for PostgreSQL and RabbitMQ |
| `energy-api/`            | Spring Boot REST API. Reads from the database. |
| `energy-gui/`            | JavaFX desktop application that calls the REST API. |
| `energy-producer/`       | Sends production messages to RabbitMQ. Uses the open-meteo Weather API. |
| `energy-user/`           | Sends usage messages to RabbitMQ. Uses the time of day. |
| `usage-service/`         | Consumes production and usage messages and updates the `energy_usage` table. |
| `percentage-service/`    | Listens for usage updates and recalculates the `current_percentage` table. |

## Start order

1. Start Docker services:
   `cd docker && docker compose up -d`
2. Start REST API:
   `cd energy-api && ./mvnw spring-boot:run`
3. Start Usage Service:
   `cd usage-service && ./mvnw spring-boot:run`
4. Start Percentage Service:
   `cd percentage-service && ./mvnw spring-boot:run`
5. Start Energy Producer:
   `cd energy-producer && ./mvnw spring-boot:run`
6. Start Energy User:
   `cd energy-user && ./mvnw spring-boot:run`
7. Start JavaFX GUI:
   `cd energy-gui && mvn clean javafx:run`

A detailed step by step guide for IntelliJ and Docker is in [ANLEITUNG.md](ANLEITUNG.md).

## Endpoints

The REST API listens on `http://localhost:8080`:

* `GET /energy/current` – current percentage data
* `GET /energy/historical?start=2025-01-10T00:00:00&end=2025-01-10T23:00:00` – usage history

## Architecture

```
+------------------+        +-------------------+
| energy-producer  |--->    |                   |
+------------------+    \   |                   |
                         \  | RabbitMQ          |
+------------------+    /   | (energy_messages) |
| energy-user      |---/    |                   |
+------------------+        +---------+---------+
                                      |
                                      v
                            +---------------------+
                            | usage-service       |
                            | (reads messages,    |
                            | writes energy_usage)|
                            +----------+----------+
                                       |
                                       v
                          +--------------------------+
                          | RabbitMQ (usage_updates) |
                          +-------------+------------+
                                        |
                                        v
                           +------------------------+
                           | percentage-service     |
                           | (writes percentage)    |
                           +------------+-----------+
                                        |
                                        v
                              +-------------------+
                              | PostgreSQL        |
                              | - energy_usage    |
                              | - current_percentage
                              +---------+---------+
                                        ^
                                        |
                              +-------------------+
                              | energy-api        |
                              | (REST endpoints)  |
                              +---------+---------+
                                        ^
                                        |
                              +-------------------+
                              | energy-gui        |
                              +-------------------+
```
