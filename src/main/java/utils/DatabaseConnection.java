/*
 * Klasi gia ti diaxeirisi tis syndesis me ti vasi dedomenwn MySQL tou XAMPP
 * xrisimopoiei to JDBC driver gia na syndesei tin efarmogi me ton MySQL server pou trexei topika sto XAMPP
*/
package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Vasikes parametroi syndesis sti vasi
    private static final String URL = "jdbc:mysql://localhost:3306/concert_venue_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Statiki metavliti gia tin syndesi
    private static Connection connection = null;
    
    // Methodos gia ti dimiourgia/epistrofi tis syndesis
    public static Connection getConnection() throws SQLException {
        try {
            // Elegxos an i syndesi den yparxei i einai kleisti
            if (connection == null || connection.isClosed()) {
                // Fortosi tou driver tis MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Prosthiki parametron asfaleias kai xronis zonis sto URL
                String fullUrl = URL + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                
                // Dimiourgia neas syndesis
                connection = DriverManager.getConnection(fullUrl, USERNAME, PASSWORD);
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }
    
    // Methodos gia to kleisimo tis syndesis
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}