import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Restaurant Reservation System");

        // buttons
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // styling
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
        registerButton.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");

        loginButton.setOnAction(e -> {
            Login login = new Login();
            login.show();
        });

        registerButton.setOnAction(e -> {
            Register register = new Register();
            register.show();
        });

        // layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f4f4;");
        layout.getChildren().addAll(loginButton, registerButton);

        // scene
        Scene scene = new Scene(layout, 550, 450);
        scene.setFill(Color.WHITE);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}