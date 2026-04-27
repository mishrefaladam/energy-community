package com.example.energy_gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main extends Application {

    private Label currentDataLabel;
    private TextArea historicalDataArea;

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
            String response = sendGetRequest("http://localhost:8080/energy/current");
            currentDataLabel.setText(response);
        } catch (Exception e) {
            currentDataLabel.setText("Error: Could not load current data. Is the API running?");
        }
    }

    private void loadHistoricalData(String start, String end) {
        try {
            String url = "http://localhost:8080/energy/historical?start=" + start + "&end=" + end;
            String response = sendGetRequest(url);
            historicalDataArea.setText(response);
        } catch (Exception e) {
            historicalDataArea.setText("Error: Could not load historical data. Is the API running?");
        }
    }

    private String sendGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        );

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }

    public static void main(String[] args) {
        launch();
    }
}