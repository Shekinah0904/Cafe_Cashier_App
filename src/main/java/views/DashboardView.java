package views;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardView {

    private VBox view;

    public DashboardView(Stage stage) {

        Button products = new Button("Products");
        Button logout = new Button("Logout");

        logout.setOnAction(e -> {
            AdminLoginView loginView = new AdminLoginView(stage);
            stage.getScene().setRoot(loginView.getView());
            stage.setTitle("Cafe App - Login");
        });

        view = new VBox(10, products, logout);
        view.setStyle("-fx-padding: 20;");
    }

    public VBox getView() {
        return view;
    }
}