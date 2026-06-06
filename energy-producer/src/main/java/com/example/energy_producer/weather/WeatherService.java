package com.example.energy_producer.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calls the open-meteo weather API and returns the current cloud cover
 * for the configured location. Lower cloud cover means more sun.
 */
@Service
public class WeatherService {

    private final double latitude;
    private final double longitude;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public WeatherService(
            @Value("${producer.weather.latitude}") double latitude,
            @Value("${producer.weather.longitude}") double longitude
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns a sun factor between 0.0 (full cloud cover) and 1.0 (no clouds).
     * If the weather api cannot be reached, returns a neutral value of 0.5.
     */
    public double getSunFactor() {
        try {
            String url = "https://api.open-meteo.com/v1/forecast"
                    + "?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&current=cloud_cover";
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET().build();

            HttpResponse<String> response = httpClient.send(getRequest,
                    HttpResponse.BodyHandlers.ofString());

            // simple extraction so we do not need an extra json library
            Pattern pattern = Pattern.compile("\"cloud_cover\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
            Matcher matcher = pattern.matcher(response.body());
            if (matcher.find()) {
                double cloudCover = Double.parseDouble(matcher.group(1));
                double clamped = Math.max(0.0, Math.min(100.0, cloudCover));
                return 1.0 - (clamped / 100.0);
            }
            return 0.5;
        } catch (Exception exception) {
            System.err.println("Could not reach weather api: " + exception.getMessage());
            return 0.5;
        }
    }
}
