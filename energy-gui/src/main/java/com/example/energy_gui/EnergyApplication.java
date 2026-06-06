package com.example.energy_gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EnergyApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader
                = new FXMLLoader(
                        EnergyApplication.class.
                                getResource("energy-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 560, 620);
        stage.setTitle("Energy Community");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
