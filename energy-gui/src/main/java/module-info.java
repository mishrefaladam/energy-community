//java.net.http wird für HttpClient, HttpRequest, HttpResponse gebraucht.
//Jackson wird für JSON-Verarbeitung gebraucht.
module com.example.energy_gui {
    //Java Module-Konfiguration
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

//opens com.example.energy_gui.dto erlaubt Jackson, per Reflection auf DTOs zuzugreifen.
//Ohne das kann JSON-Mapping im modularen Java-Projekt Probleme machen.
    opens com.example.energy_gui to javafx.fxml;
    opens com.example.energy_gui.dto to com.fasterxml.jackson.databind;
    exports com.example.energy_gui;
}
