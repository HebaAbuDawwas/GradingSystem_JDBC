package org.atypon;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.atypon.models.Student;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static org.atypon.constants.Constants.INVALID_PASSWORD_ERROR_MESSAGE;
import static org.atypon.constants.Constants.INVALID_USER_ERROR_MESSAGE;


public class StudentGradingClient extends Application {
    String host = "localhost";


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Student Grading System");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Welcome to Student Grading System");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);

        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            try {
                Socket socket = new Socket(host, 8000);

                // Create an output stream to the server
                ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
                Student student = new Student();
                student.setUsername(username);
                student.setPassword(password);

                toServer.writeObject(student);
                receiveMessageFromServer(socket);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().add(loginButton);

        root.getChildren().addAll(titleLabel, gridPane, buttonBox);

        Scene scene = new Scene(root, 400, 250);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void receiveMessageFromServer(Socket socket) {
        try {

            ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
            String message = (String) fromServer.readObject();
            fromServer.close();
            socket.close();
            if (message.equals(INVALID_USER_ERROR_MESSAGE) || message.equals(INVALID_PASSWORD_ERROR_MESSAGE)) {
                showAlert(message);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Student Marks");
                alert.setHeaderText("Welcome, ");
                alert.setContentText(message);
                alert.showAndWait();
            }

        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Result");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
