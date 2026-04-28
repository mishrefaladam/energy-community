package com.example.energy_gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
Do not start the JavaFX GUI with the VS Code Run button. Use Maven instead.
*/

public class Main extends Application {

    private static final String API_BASE_URL = "http://localhost:8080";

    private Label currentDataLabel;
    private TextArea historicalDataArea;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("Energy Community GUI");

        currentDataLabel = new Label("Current data not loaded yet.");
        Button refreshButton = new Button("Refresh Current Data");

        TextField startField = new TextField("2025-01-10T13:00:00");
        TextField endField = new TextField("2025-01-10T14:00:00");

        Button showDataButton = new Button("Show Historical Data");

        historicalDataArea = new TextArea();
        historicalDataArea.setEditable(false);
        historicalDataArea.setPrefHeight(180);

        refreshButton.setOnAction(e -> loadCurrentData());
        showDataButton.setOnAction(e -> loadHistoricalData(startField.getText(), endField.getText()));

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 15;");
        root.getChildren().addAll(
                titleLabel,
                refreshButton,
                currentDataLabel,
                new Label("Start:"),
                startField,
                new Label("End:"),
                endField,
                showDataButton,
                historicalDataArea
        );

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.setTitle("Energy GUI");
        stage.show();
    }

    private void loadCurrentData() {
        try {
            String response = sendGetRequest(API_BASE_URL + "/energy/current");
            currentDataLabel.setText(response);
        } catch (IOException e) {
            currentDataLabel.setText("Error loading current data: " + getReadableErrorMessage(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            currentDataLabel.setText("Error loading current data: " + getReadableErrorMessage(e));
        }
    }

    private void loadHistoricalData(String start, String end) {
        try {
            String url = API_BASE_URL + "/energy/historical?start=" + start + "&end=" + end;
            String response = sendGetRequest(url);
            historicalDataArea.setText(response);
        } catch (IOException e) {
            historicalDataArea.setText("Error loading historical data: " + getReadableErrorMessage(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            historicalDataArea.setText("Error loading historical data: " + getReadableErrorMessage(e));
        }
    }

    private String sendGetRequest(String urlString) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();

        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("API returned HTTP " + statusCode);
        }

        return response.body();
    }

    private String getReadableErrorMessage(Exception exception) {
        String baseMessage = "Could not reach the API. Make sure energy-api is running on http://localhost:8080.";
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return baseMessage;
        }
        return baseMessage + " Details: " + message;
    }

    public static void main(String[] args) {
        launch();
    }
}