package views;

import db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SignupView {

    private VBox view;

    public SignupView(Stage stage) {

        Label title = new Label("Create Account");

        TextField idField = new TextField();
        idField.setPromptText("User ID");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label message = new Label();

        Button createBtn = new Button("Create Account");
        Button backBtn = new Button("Back to Login");

        createBtn.setOnAction(e -> {

            String id = idField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (id.isEmpty() || username.isEmpty() || password.isEmpty()) {

                message.setText("Please fill all fields.");
                return;
            }

            try {

                Connection conn = Database.connect();

                if (conn == null) {

                    message.setText("Database not connected.");
                    return;
                }

                String sql =
                        "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, id);
                stmt.setString(2, username);
                stmt.setString(3, password);

                stmt.executeUpdate();

                message.setText("Account created successfully!");

                idField.clear();
                usernameField.clear();
                passwordField.clear();

            } catch (Exception ex) {

                message.setText("Error: " + ex.getMessage());

                ex.printStackTrace();
            }
        });

        backBtn.setOnAction(e -> {

            AdminLoginView login = new AdminLoginView(stage);

            Scene scene = new Scene(login.getView(), 300, 250);

            stage.setScene(scene);
            stage.setTitle("Admin Login");
        });

        view = new VBox(
                10,
                title,
                idField,
                usernameField,
                passwordField,
                createBtn,
                backBtn,
                message
        );

        view.setPadding(new Insets(20));
    }

    public VBox getView() {

        return view;
    }
}