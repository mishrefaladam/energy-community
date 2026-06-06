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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {

    private static final String API_BASE_URL = "http://localhost:8080";

    private TextArea currentDataArea;
    private TextArea historicalDataArea;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("Energy Community GUI");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        currentDataArea = new TextArea("Current data not loaded yet.");
        currentDataArea.setEditable(false);
        currentDataArea.setWrapText(true);
        currentDataArea.setPrefHeight(90);

        Button refreshButton = new Button("Refresh Current Data");

        TextField startField = new TextField("2025-01-10T13:00:00");
        TextField endField = new TextField("2025-01-10T14:00:00");

        Button showDataButton = new Button("Show Historical Data");

        historicalDataArea = new TextArea();
        historicalDataArea.setEditable(false);
        historicalDataArea.setWrapText(true);
        historicalDataArea.setPrefHeight(180);

        refreshButton.setOnAction(e -> loadCurrentData());
        showDataButton.setOnAction(e -> loadHistoricalData(startField.getText(), endField.getText()));

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 15;");
        root.getChildren().addAll(
                titleLabel,
                refreshButton,
                currentDataArea,
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
            currentDataArea.setText(formatCurrentData(response));
        } catch (IOException e) {
            currentDataArea.setText("Error loading current data:\n" + getReadableErrorMessage(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            currentDataArea.setText("Error loading current data:\n" + getReadableErrorMessage(e));
        }
    }

    private void loadHistoricalData(String start, String end) {
        try {
            String url = API_BASE_URL + "/energy/historical?start=" + start + "&end=" + end;
            String response = sendGetRequest(url);
            historicalDataArea.setText(formatHistoricalData(response));
        } catch (IOException e) {
            historicalDataArea.setText("Error loading historical data:\n" + getReadableErrorMessage(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            historicalDataArea.setText("Error loading historical data:\n" + getReadableErrorMessage(e));
        }
    }

    private String formatCurrentData(String jsonResponse) {
        return String.join("\n",
                "Hour: " + extractJsonValue(jsonResponse, "hour"),
                "Community Pool: " + extractJsonValue(jsonResponse, "communityDepleted") + "% used",
                "Grid Portion: " + extractJsonValue(jsonResponse, "gridPortion") + "%"
        );
    }

    private String formatHistoricalData(String jsonResponse) {
        String trimmedResponse = jsonResponse == null ? "" : jsonResponse.trim();

        if (trimmedResponse.startsWith("[")) {
            List<String> entries = splitJsonObjects(trimmedResponse);
            if (entries.isEmpty()) {
                return formatHistoricalEntry(trimmedResponse);
            }

            StringBuilder result = new StringBuilder();
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) {
                    result.append("\n\n");
                }
                result.append(formatHistoricalEntry(entries.get(i)));
            }
            return result.toString();
        }

        return formatHistoricalEntry(trimmedResponse);
    }

    private String formatHistoricalEntry(String jsonResponse) {
        return String.join("\n",
                "Hour: " + extractJsonValue(jsonResponse, "hour"),
                "Community produced: " + extractJsonValue(jsonResponse, "communityProduced") + " kWh",
                "Community used: " + extractJsonValue(jsonResponse, "communityUsed") + " kWh",
                "Grid used: " + extractJsonValue(jsonResponse, "gridUsed") + " kWh"
        );
    }

    private List<String> splitJsonObjects(String jsonResponse) {
        List<String> entries = new ArrayList<>();
        int depth = 0;
        int objectStart = -1;

        for (int i = 0; i < jsonResponse.length(); i++) {
            char character = jsonResponse.charAt(i);

            if (character == '{') {
                if (depth == 0) {
                    objectStart = i;
                }
                depth++;
            } else if (character == '}') {
                depth--;
                if (depth == 0 && objectStart >= 0) {
                    entries.add(jsonResponse.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            }
        }

        return entries;
    }

    private String extractJsonValue(String jsonResponse, String key) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return "n/a";
        }

        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\"([^\"]*)\"|[-0-9.]+)");
        Matcher matcher = pattern.matcher(jsonResponse);

        if (!matcher.find()) {
            return "n/a";
        }

        String quotedValue = matcher.group(2);
        if (quotedValue != null) {
            return quotedValue;
        }

        return matcher.group(1);
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