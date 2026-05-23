package views;

import db.Database;
import util.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminLoginView {

    private VBox view;

    public AdminLoginView(Stage stage) {

        Label title = new Label("Admin Login");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label message = new Label();

        Button loginBtn = new Button("Login");
        Button signupBtn = new Button("Sign Up");

        loginBtn.setOnAction(e -> {

            String usernameInput = usernameField.getText();
            String password = passwordField.getText();

            try (Connection conn = Database.connect()) {

                if (conn == null) {
                    message.setText("Database not connected");
                    return;
                }

                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM users WHERE username = ? AND password = ?"
                );

                stmt.setString(1, usernameInput);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {

                    String dbUsername = rs.getString("username");
                    Session.currentUsername = dbUsername;

                    // CLOSE LOGIN WINDOW
                    stage.close();

                    // OPEN NEW APP WINDOW
                    Stage mainStage = new Stage();
                    MainView main = new MainView(mainStage);

                    Scene scene = new Scene(main.getView(), 700, 500);

                    mainStage.setScene(scene);
                    mainStage.setTitle("My App - Dashboard");
                    mainStage.show();

                } else {
                    message.setText("Invalid credentials!");
                }

            } catch (Exception ex) {
                message.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        signupBtn.setOnAction(e -> {
            SignupView signup = new SignupView(stage);
            Scene scene = new Scene(signup.getView(), 350, 300);

            stage.setScene(scene);
            stage.setTitle("Sign Up");
        });

        view = new VBox(10,
                title,
                usernameField,
                passwordField,
                loginBtn,
                signupBtn,
                message
        );

        view.setPadding(new Insets(20));
    }

    public VBox getView() {
        return view;
    }
}