package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import views.AdminLoginView;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        AdminLoginView login = new AdminLoginView(stage);

        Scene scene = new Scene(login.getView(), 300, 250);

        stage.setTitle("Cafe App - Admin Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {

        launch();
    }
}