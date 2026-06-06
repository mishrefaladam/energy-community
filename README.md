# Energy Community

Distributed Systems semester project.

The project simulates an energy community with RabbitMQ, PostgreSQL,
Spring Boot services, a REST API and a JavaFX GUI.

## Components

- `docker/` starts PostgreSQL and RabbitMQ
- `energy-api/` exposes REST endpoints and reads from PostgreSQL
- `usage-service/` consumes energy messages and updates usage data
- `percentage-service/` calculates current percentage data
- `energy-producer/` sends producer messages to RabbitMQ
- `energy-user/` sends user messages to RabbitMQ
- `energy-gui/` shows current and historical data from the REST API

## Start Order

1. Start Docker services

```bash
cd docker
docker compose up -d
```

2. Start REST API

```bash
cd energy-api
./mvnw spring-boot:run
```

3. Start Usage Service

```bash
cd usage-service
./mvnw spring-boot:run
```

4. Start Percentage Service

```bash
cd percentage-service
./mvnw spring-boot:run
```

5. Start Energy Producer

```bash
cd energy-producer
./mvnw spring-boot:run
```

6. Start Energy User

```bash
cd energy-user
./mvnw spring-boot:run
```

7. Start JavaFX GUI

```bash
cd energy-gui
mvn clean javafx:run
```

## REST Endpoints

- `GET http://localhost:8080/energy/current`
- `GET http://localhost:8080/energy/historical?start=2025-01-10T13:00:00&end=2025-01-10T14:00:00`
