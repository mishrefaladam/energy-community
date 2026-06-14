package com.example.energy_gui;

//Diese Imports zeigen direkt, dass die GUI REST-Aufrufe macht und JSON verarbeitet.
import com.example.energy_gui.dto.CurrentEnergyDto;
import com.example.energy_gui.dto.HistoricalEnergyDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

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

//Controller für die UI-Logik.
//Die GUI spricht nicht direkt mit RabbitMQ oder PostgreSQL, sondern nur mit der energy-api.
public class EnergyController {

    //Die GUI erwartet, dass die REST API unter http://localhost:8080 läuft.
    //API_FORMAT wird genutzt, um Zeitwerte passend für die URL zu formatieren.
    private static final String API_BASE_URL = "http://localhost:8080";
    private static final DateTimeFormatter API_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    //Diese Felder sind mit den fx:ids aus der FXML-Datei verbunden. Dadurch kann der Controller die UI-Elemente verändern.
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

    //Die GUI nutzt HttpClient, um HTTP-Anfragen an die REST API zu schicken.
    //ObjectMapper wandelt JSON-Antworten in DTOs um.
    //JavaTimeModule ist wichtig, weil die DTOs LocalDateTime verwenden.
    private final HttpClient client = HttpClient.newBuilder().build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String formatKwh(double value) {
        //Einheitliche Anzeige mit drei Nachkommastellen für kWh-Werte.
        return String.format(Locale.US, "%.3f", value);
    }

    //initialize() wird automatisch aufgerufen, nachdem die FXML geladen wurde. Hier werden die Default-Werte eingerichtet.
    @FXML
    public void initialize() {
        sp_startHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        sp_endHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 23));

        LocalDate today = LocalDate.now();
        dp_start.setValue(today.minusDays(1));
        dp_end.setValue(today);
    }

    //Wenn der Benutzer auf refresh klickt, wird onRefreshClicked() ausgeführt. (siehe energy-view.fxml)
    @FXML
    protected void onRefreshClicked() {
        //Hier wird der REST-Endpunkt gebaut: ein GET-Request an die energy-api.
        try {
            String url = API_BASE_URL + "/energy/current";
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET().build();

            //Die GUI sendet den Request und bekommt die Antwort als Text/String zurück.
            //Dieser Text ist normalerweise JSON.
            HttpResponse<String> response = client.send(getRequest,
                    HttpResponse.BodyHandlers.ofString());
            //Wenn der Server nicht mit 200 OK antwortet, zeigt die GUI eine Fehlermeldung an und bricht ab.
            if (response.statusCode() != 200) {
                lb_status.setText("Error: server returned status " + response.statusCode());
                return;
            }
            //Die REST API gibt JSON zurück.
            //Die GUI (mapper.readValue(...)) wandelt dieses JSON in CurrentEnergyDto um und zeigt die Daten in Labels an.
            CurrentEnergyDto current = mapper.readValue(response.body(), CurrentEnergyDto.class);
            lb_communityPool.setText(String.format("%.2f%% used", current.getCommunityDepleted()));
            lb_gridPortion.setText(String.format("%.2f%%", current.getGridPortion()));
        } catch (Exception exception) {
            System.err.println("Something went wrong during get"
                    + exception.getMessage());
            lb_status.setText("Error: " + exception.getMessage());
        }
    }

    //Hier ruft die GUI den historischen REST-Endpunkt mit Query-Parametern auf.
    //Wenn der Benutzer auf show data klickt, wird onShowDataClicked() ausgeführt. (siehe energy-view.fxml)
    @FXML
    protected void onShowDataClicked() {
        //Die GUI liest das Datum aus DatePicker und die Stunde aus Spinner.
        //Daraus entstehen start und end.
        try {
            LocalDateTime start = dp_start.getValue().atTime(sp_startHour.getValue(), 0);
            LocalDateTime end = dp_end.getValue().atTime(sp_endHour.getValue(), 0);
            //Hier wird die URL für den historischen Endpoint gebaut.
            //URLEncoder.encode(...) sorgt dafür, dass Sonderzeichen in der URL korrekt übertragen werden.
            String startEncoded = URLEncoder.encode(start.format(API_FORMAT), StandardCharsets.UTF_8);
            String endEncoded = URLEncoder.encode(end.format(API_FORMAT), StandardCharsets.UTF_8);
            String url = API_BASE_URL + "/energy/historical?start=" + startEncoded + "&end=" + endEncoded;
            //Hier wird wieder ein GET-Request gebaut.
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET().build();
            //Die GUI sendet den Request an die API und erhält eine JSON-Antwort als String.
            HttpResponse<String> response = client.send(getRequest,
                    HttpResponse.BodyHandlers.ofString());
            //Wenn die API nicht mit 200 OK antwortet, zeigt die GUI eine Fehlermeldung.
            if (response.statusCode() != 200) {
                lb_status.setText("Error: server returned status " + response.statusCode());
                return;
            }
            //Die API gibt eine JSON-Liste zurück.
            //Die GUI (ObjectMapper) wandelt sie in eine Liste von HistoricalEnergyDto um.
            List<HistoricalEnergyDto> data = mapper.readValue(
                    response.body(),
                    new TypeReference<List<HistoricalEnergyDto>>() {}
            );

            //Die GUI summiert die historischen Daten und zeigt die Gesamtwerte in den Labels an.
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
        } catch (Exception exception) {
            System.err.println("Something went wrong during get"
                    + exception.getMessage());
            lb_status.setText("Error: " + exception.getMessage());
        }
    }

}