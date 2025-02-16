import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class Login {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "053307"; // Ganti sesuai password MySQL Anda

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Login");

        // Labels
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // TextFields
        TextField userField = new TextField();
        userField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        PasswordField passField = new PasswordField();
        passField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 5; -fx-border-radius: 5;");

        // Buttons
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
        Button backButton = new Button("← Back");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");

        // Button actions
        loginButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and Password cannot be empty!");
            } else {
                if (authenticateUser(username, password)) {
                    errorLabel.setText("");
                    
                    if ("admin".equals(username)) {
                        ManageReservation manageReservation = new ManageReservation();
                        manageReservation.show();
                    } else {
                        Reservation reservation = new Reservation();
                        reservation.show();
                    }
                    stage.close();
                } else {
                    errorLabel.setText("Invalid username or password!");
                }
            }
        });

        backButton.setOnAction(e -> stage.close());

        // Layout
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f4f4;");
        layout.getChildren().addAll(userLabel, userField, passLabel, passField, errorLabel, loginButton, backButton);

        // Scene
        Scene scene = new Scene(layout, 550, 450);
        stage.setScene(scene);
        stage.show();
    }

    private boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Jika ada hasil, berarti user valid
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
