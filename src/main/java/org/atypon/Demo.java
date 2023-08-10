package org.atypon;

import javafx.application.Application;
import javafx.stage.Stage;

public class Demo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        StudentGradingSystemApp app = new StudentGradingSystemApp(primaryStage);
        app.start();
    }
}
