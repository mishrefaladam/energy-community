package com.example.energy_gui;

import com.example.energy_gui.dto.CurrentEnergyDto;
import com.example.energy_gui.dto.HistoricalEnergyDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class EnergyController {

    private static final String API_BASE_URL = "http://localhost:8080";
    private static final DateTimeFormatter API_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @FXML
    private Label lb_communityPool;

    @FXML
    private Label lb_gridPortion;

    @FXML
    private Button btn_refresh;

    @FXML
    private DatePicker dp_start;

    @FXML
    private Spinner<Integer> sp_startHour;

    @FXML
    private DatePicker dp_end;

    @FXML
    private Spinner<Integer> sp_endHour;

    @FXML
    private Button btn_showData;

    @FXML
    private Label lb_communityProduced;

    @FXML
    private Label lb_communityUsed;

    @FXML
    private Label lb_gridUsed;

    @FXML
    private Label lb_status;

    @FXML
    private TableView<HistoricalEnergyDto> tbl_history;

    @FXML
    private TableColumn<HistoricalEnergyDto, String> col_hour;

    @FXML
    private TableColumn<HistoricalEnergyDto, Number> col_produced;

    @FXML
    private TableColumn<HistoricalEnergyDto, Number> col_used;

    @FXML
    private TableColumn<HistoricalEnergyDto, Number> col_gridUsed;

    private final HttpClient client = HttpClient.newBuilder().build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String formatKwh(double value) {
        return String.format(Locale.US, "%.3f", value);
    }

    @FXML
    public void initialize() {
        sp_startHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        sp_endHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 23));

        LocalDate today = LocalDate.now();
        dp_start.setValue(today.minusDays(1));
        dp_end.setValue(today);

        col_hour.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getHour() != null
                                ? data.getValue().getHour().toString()
                                : ""
                )
        );
        col_produced.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getCommunityProduced())
        );
        col_used.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getCommunityUsed())
        );
        col_gridUsed.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getGridUsed())
        );

        col_produced.setCellFactory(column -> createKwhCell());
        col_used.setCellFactory(column -> createKwhCell());
        col_gridUsed.setCellFactory(column -> createKwhCell());
    }

    private TableCell<HistoricalEnergyDto, Number> createKwhCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : formatKwh(value.doubleValue()));
            }
        };
    }

    @FXML
    protected void onRefreshClicked() {
        try {
            String url = API_BASE_URL + "/energy/current";
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET().build();

            HttpResponse<String> response = client.send(getRequest,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                lb_status.setText("Error: server returned status " + response.statusCode());
                return;
            }

            CurrentEnergyDto current = mapper.readValue(response.body(), CurrentEnergyDto.class);
            lb_communityPool.setText(String.format("%.2f%% used", current.getCommunityDepleted()));
            lb_gridPortion.setText(String.format("%.2f%%", current.getGridPortion()));
            lb_status.setText("Current data refreshed for hour " + current.getHour());
        } catch (Exception exception) {
            System.err.println("Something went wrong during get"
                    + exception.getMessage());
            lb_status.setText("Error: " + exception.getMessage());
        }
    }

    @FXML
    protected void onShowDataClicked() {
        try {
            LocalDateTime start = dp_start.getValue().atTime(sp_startHour.getValue(), 0);
            LocalDateTime end = dp_end.getValue().atTime(sp_endHour.getValue(), 0);

            String startEncoded = URLEncoder.encode(start.format(API_FORMAT), StandardCharsets.UTF_8);
            String endEncoded = URLEncoder.encode(end.format(API_FORMAT), StandardCharsets.UTF_8);
            String url = API_BASE_URL + "/energy/historical?start=" + startEncoded + "&end=" + endEncoded;

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET().build();

            HttpResponse<String> response = client.send(getRequest,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                lb_status.setText("Error: server returned status " + response.statusCode());
                return;
            }

            List<HistoricalEnergyDto> data = mapper.readValue(
                    response.body(),
                    new TypeReference<List<HistoricalEnergyDto>>() {}
            );

            double producedSum = 0.0;
            double usedSum = 0.0;
            double gridSum = 0.0;
            for (HistoricalEnergyDto entry : data) {
                producedSum += entry.getCommunityProduced();
                usedSum += entry.getCommunityUsed();
                gridSum += entry.getGridUsed();
            }

            lb_communityProduced.setText(formatKwh(producedSum) + " kWh");
            lb_communityUsed.setText(formatKwh(usedSum) + " kWh");
            lb_gridUsed.setText(formatKwh(gridSum) + " kWh");

            ObservableList<HistoricalEnergyDto> rows = FXCollections.observableArrayList(data);
            tbl_history.setItems(rows);

            lb_status.setText("Loaded " + data.size() + " hourly entries.");
        } catch (Exception exception) {
            System.err.println("Something went wrong during get"
                    + exception.getMessage());
            lb_status.setText("Error: " + exception.getMessage());
        }
    }

}
