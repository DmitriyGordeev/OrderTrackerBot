import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler {

    private Connection connection;
    private Statement  statement;

    public Connection getConnection() { return connection; }
    public Statement getStatement() { return statement; }


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

    ArrayList<SaleRecord> retreiveData(ResultSet resultSet) throws SQLException {
        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        while(resultSet.next())
        {
            SaleRecord saleRecord = new SaleRecord();
            saleRecord.id = resultSet.getInt("id");
            saleRecord.userId = resultSet.getInt("userId");
            saleRecord.username = resultSet.getString("username");
            saleRecord.message = resultSet.getString("message");

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            try {
                saleRecord.date = dateFormat.parse(resultSet.getString("date"));
            }
            catch(ParseException e) { continue; }
            output.add(saleRecord);
        }

        return output;
    }

    ArrayList<SaleRecord> getRecords() {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM sales");
            output = retreiveData(resultSet);
        }
        catch(SQLException e) {}

        return output;
    }

    ArrayList<SaleRecord> getRecords(String date) {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM sales where date='" + date + "'");
            output = retreiveData(resultSet);
        }
        catch(SQLException e) {}

        return output;
    }





}