import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static void main(String[] args) {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            // coba koneksi ke db
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Koneksi ke database berhasil!");
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Driver tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
