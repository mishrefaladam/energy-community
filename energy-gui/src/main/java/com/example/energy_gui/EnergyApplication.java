package com.example.energy_gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

//Die Klasse startet die JavaFX-Anwendung. start(Stage stage) ist die Hauptmethode für das GUI-Fenster.
public class EnergyApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //Hier wird die Datei energy-view.fxml geladen und daraus eine Scene erstellt.
        FXMLLoader fxmlLoader
                = new FXMLLoader(
                        EnergyApplication.class.
                                getResource("energy-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 560, 430);
        //Hier wird der Fenstertitel gesetzt, die Scene ins Fenster gelegt und das Fenster angezeigt.
        stage.setTitle("Energy Community");
        stage.setScene(scene);
        stage.show();
    }
    //launch() startet die JavaFX-Laufzeit. Danach wird start(...) aufgerufen.
    public static void main(String[] args) {
        launch();
    }
}
