import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class ManageReservation {
    private VBox reservationList;

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Manage Reservations");

        // Back Button (kecil di kiri atas)
        Button backButton = new Button("← Logout");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5;");
        backButton.setOnAction(e -> stage.close());

        // Layout untuk back button
        HBox topBar = new HBox(backButton);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_LEFT);

        reservationList = new VBox(10);
        reservationList.setPadding(new Insets(10));

        loadReservations();

        ScrollPane scrollPane = new ScrollPane(reservationList);
        scrollPane.setFitToWidth(true);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(topBar, new Label("Reservations:"), scrollPane);

        Scene scene = new Scene(layout, 600, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void loadReservations() {
        reservationList.getChildren().clear();
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, name, table_number FROM reservations");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int tableNumber = rs.getInt("table_number");

                HBox reservationItem = createReservationItem(id, name, tableNumber);
                reservationList.getChildren().add(reservationItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createReservationItem(int id, String name, int tableNumber) {
        Label nameLabel = new Label(name + " - Table " + tableNumber);
        nameLabel.setMinWidth(200);
        nameLabel.setStyle("-fx-font-size: 14px;");

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");

        editButton.setOnAction(e -> showEditDialog(id, name, tableNumber));
        deleteButton.setOnAction(e -> deleteReservation(id));

        HBox itemBox = new HBox(10, nameLabel, editButton, deleteButton);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        return itemBox;
    }

    private void showEditDialog(int id, String oldName, int oldTableNumber) {
        Stage editStage = new Stage();
        editStage.setTitle("Edit Reservation");

        TextField nameField = new TextField(oldName);
        TextField tableField = new TextField(String.valueOf(oldTableNumber));

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
        saveButton.setOnAction(e -> {
            updateReservation(id, nameField.getText(), Integer.parseInt(tableField.getText()));
            editStage.close();
        });

        VBox editLayout = new VBox(10, new Label("Name:"), nameField, new Label("Table Number:"), tableField, saveButton);
        editLayout.setPadding(new Insets(20));

        Scene scene = new Scene(editLayout, 300, 200);
        editStage.setScene(scene);
        editStage.show();
    }

    private void updateReservation(int id, String newName, int newTableNumber) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // Mulai transaksi

            // Ambil nomor meja lama sebelum diupdate
            PreparedStatement getOldTableStmt = conn.prepareStatement("SELECT table_number FROM reservations WHERE id = ?");
            getOldTableStmt.setInt(1, id);
            ResultSet rs = getOldTableStmt.executeQuery();
            int oldTableNumber = -1;
            if (rs.next()) {
                oldTableNumber = rs.getInt("table_number");
            }

            // Update reservasi dengan data baru
            PreparedStatement updateReservationStmt = conn.prepareStatement("UPDATE reservations SET name = ?, table_number = ? WHERE id = ?");
            updateReservationStmt.setString(1, newName);
            updateReservationStmt.setInt(2, newTableNumber);
            updateReservationStmt.setInt(3, id);
            updateReservationStmt.executeUpdate();

            // Jika nomor meja berubah, perbarui status tabel
            if (oldTableNumber != -1 && oldTableNumber != newTableNumber) {
                // Set meja lama menjadi "available"
                PreparedStatement setOldTableAvailableStmt = conn.prepareStatement("UPDATE tables SET status = 'available' WHERE table_number = ?");
                setOldTableAvailableStmt.setInt(1, oldTableNumber);
                setOldTableAvailableStmt.executeUpdate();

                // Set meja baru menjadi "reserved"
                PreparedStatement setNewTableReservedStmt = conn.prepareStatement("UPDATE tables SET status = 'reserved' WHERE table_number = ?");
                setNewTableReservedStmt.setInt(1, newTableNumber);
                setNewTableReservedStmt.executeUpdate();
            }

            conn.commit(); // Commit transaksi
            loadReservations(); // Refresh tampilan
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteReservation(int id) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // Mulai transaksi

            // Ambil nomor meja sebelum dihapus
            PreparedStatement getTableStmt = conn.prepareStatement("SELECT table_number FROM reservations WHERE id = ?");
            getTableStmt.setInt(1, id);
            ResultSet rs = getTableStmt.executeQuery();
            int tableNumber = -1;
            if (rs.next()) {
                tableNumber = rs.getInt("table_number");
            }

            // Hapus reservasi
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM reservations WHERE id = ?");
            deleteStmt.setInt(1, id);
            deleteStmt.executeUpdate();

            // Ubah status meja yang sebelumnya digunakan menjadi "available"
            if (tableNumber != -1) {
                PreparedStatement updateTableStatusStmt = conn.prepareStatement("UPDATE tables SET status = 'available' WHERE table_number = ?");
                updateTableStatusStmt.setInt(1, tableNumber);
                updateTableStatusStmt.executeUpdate();
            }

            conn.commit(); // Commit transaksi
            loadReservations(); // Refresh tampilan
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}