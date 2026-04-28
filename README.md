# Energy Community - Intermediate Hand-In

## Project Description

This project is an intermediate hand-in for the Distributed Systems course.
It demonstrates a simple Energy Community system with a Spring Boot REST API and a JavaFX GUI.

## Components

The repository contains two independently startable applications:

1. energy-api
	- Spring Boot REST API
	- Provides structured example data
	- No database for the intermediate hand-in
	- No RabbitMQ for the intermediate hand-in

2. energy-gui
	- JavaFX GUI
	- Calls the REST API using HTTP
	- Displays current and historical energy data

## Start Order

Start the REST API first.
Then start the JavaFX GUI.

## Start REST API

```bash
cd energy-api
mvn clean spring-boot:run
```

## Start JavaFX GUI

```bash
cd energy-gui
mvn clean javafx:run
```

```md
Do not start the JavaFX GUI with the VS Code Run button. Use Maven instead.
```

## API Endpoints

The GUI calls these endpoints on the API:

- `GET http://localhost:8080/energy/current`
- `GET http://localhost:8080/energy/historical?start=2025-01-10T13:00:00&end=2025-01-10T14:00:00`

## Project Structure

The repository keeps the applications separate:

- `energy-api/` contains the Spring Boot backend
- `energy-gui/` contains the JavaFX frontend
- `README.md` documents the full project
- `.gitignore` excludes generated and IDE files