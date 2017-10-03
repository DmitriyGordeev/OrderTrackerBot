import java.sql.*;

public class DatabaseHandler {

    private Connection connection;
    private Statement  statement;

    public boolean connect(String url, String user, String password) {
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement  = connection.createStatement();
        }
        catch(SQLException e) {
            return false;
        }
        return true;
    }

}
