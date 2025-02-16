import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Reservation {
    private HashMap<Integer, ToggleButton> tableButtons = new HashMap<>();
    private GridPane tableGrid;
    private ToggleButton selectedButton = null;

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Make Reservation");

        // Labels
        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        Label tableLabel = new Label("Select Table:");
        tableLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 12px;");

        // Input Field
        TextField nameField = new TextField();
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-radius: 5; -fx-border-radius: 5;");

        // Grid for table selection
        tableGrid = new GridPane();
        tableGrid.setHgap(10);
        tableGrid.setVgap(10);
        tableGrid.setPadding(new Insets(10));
        tableGrid.setAlignment(Pos.CENTER);

        ToggleGroup tableGroup = new ToggleGroup();
        loadTableData(tableGroup);

        // Buttons
        Button reserveButton = new Button("Reserve");
        reserveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
        Button backButton = new Button("← Logout");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5;");

        reserveButton.setOnAction(e -> {
            if (nameField.getText().trim().isEmpty() || tableGroup.getSelectedToggle() == null) {
                errorLabel.setText("All fields must be filled!");
                successLabel.setText("");
            } else {
                String name = nameField.getText().trim();
                ToggleButton selectedTable = (ToggleButton) tableGroup.getSelectedToggle();
                int tableNumber = Integer.parseInt(selectedTable.getText().replace("Table ", ""));

                // Insert reservation into database
                try (Connection conn = DatabaseHelper.getConnection()) {
                    // Insert reservation
                    PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO reservations (name, table_number) VALUES (?, ?)");
                    insertStmt.setString(1, name);
                    insertStmt.setInt(2, tableNumber);
                    insertStmt.executeUpdate();

                    // Update table status
                    PreparedStatement updateStmt = conn.prepareStatement("UPDATE tables SET status = 'reserved' WHERE table_number = ?");
                    updateStmt.setInt(1, tableNumber);
                    updateStmt.executeUpdate();

                    successLabel.setText("Reservation succeeded!");
                    errorLabel.setText("");
                    nameField.clear();
                    
                    // Refresh table data
                    loadTableData(tableGroup);
                } catch (SQLException ex) {
                    errorLabel.setText("Database error: " + ex.getMessage());
                }
            }
        });

        backButton.setOnAction(e -> stage.close());

        // Layout
        HBox topLayout = new HBox();
        topLayout.setAlignment(Pos.TOP_LEFT);
        topLayout.getChildren().add(backButton);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f4f4;");
        layout.getChildren().addAll(topLayout, nameLabel, nameField, tableLabel, tableGrid, errorLabel, reserveButton, successLabel);

        // Scene
        Scene scene = new Scene(layout, 550, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void loadTableData(ToggleGroup tableGroup) {
        tableGrid.getChildren().clear();
        tableButtons.clear();

        // Fetch tables from database
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT table_number, status FROM tables");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int tableNumber = rs.getInt("table_number");
                String status = rs.getString("status");

                ToggleButton tableButton = new ToggleButton("Table " + tableNumber);
                tableButton.setMinWidth(80);
                tableButton.setMinHeight(40);

                if (status.equals("available")) {
                    tableButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                } else {
                    tableButton.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
                    tableButton.setDisable(true);
                }

                tableButton.setToggleGroup(tableGroup);
                tableButton.setOnAction(e -> {
                    if (selectedButton != null) {
                        selectedButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    }
                    tableButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white;");
                    selectedButton = tableButton;
                });

                tableGrid.add(tableButton, (tableNumber - 1) % 5, (tableNumber - 1) / 5);
                tableButtons.put(tableNumber, tableButton);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
