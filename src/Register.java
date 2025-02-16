import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Register {
    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Register");

        // Labels
        Label userLabel = new Label("Username:");
        Label passLabel = new Label("Password:");
        Label confirmPassLabel = new Label("Confirm Password:");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        // TextFields
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        PasswordField confirmPassField = new PasswordField();

        // Buttons
        Button registerButton = new Button("Register");
        Button backButton = new Button("← Back");

        // Button styling
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");

        // Button actions
        registerButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            String confirmPassword = confirmPassField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                errorLabel.setText("Fields cannot be empty!");
            } else if (!password.equals(confirmPassword)) {
                errorLabel.setText("Passwords do not match!");
            } else {
                try (Connection conn = DatabaseHelper.getConnection()) {
                    // Check if username exists
                    PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
                    checkStmt.setString(1, username);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        errorLabel.setText("Username already taken!");
                        return;
                    }

                    // Insert new user
                    PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, password); // Password tidak di-hash
                    insertStmt.executeUpdate();

                    errorLabel.setStyle("-fx-text-fill: green;");
                    errorLabel.setText("Registration successful!");
                } catch (SQLException ex) {
                    errorLabel.setText("Database error: " + ex.getMessage());
                }
            }
        });

        backButton.setOnAction(e -> stage.close());

        // Layout
        VBox layout = new VBox(10, userLabel, userField, passLabel, passField, confirmPassLabel, confirmPassField, errorLabel, registerButton, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout, 550, 450);
        stage.setScene(scene);
        stage.show();
    }
}