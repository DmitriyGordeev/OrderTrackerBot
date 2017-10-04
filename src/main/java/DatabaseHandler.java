import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler {

    private Connection connection;
    private Statement  statement;
    private String tableName = "sales_release";

    public Connection getConnection() { return connection; }
    public Statement getStatement() { return statement; }

    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getTableName() {
        return tableName;
    }

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

    public ArrayList<SaleRecord> retreiveData(ResultSet resultSet) throws SQLException {
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

    public ArrayList<SaleRecord> getRecords() {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            output = retreiveData(resultSet);
        }
        catch(SQLException e) {}

        return output;
    }

    public ArrayList<SaleRecord> getRecords(String date) {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " where date='" + date + "'");
            output = retreiveData(resultSet);
        }
        catch(SQLException e) {}

        return output;
    }

    public ArrayList<SaleRecord> getRecordsMonth(String date) {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        String query = "SELECT * FROM " + tableName + " where date like '%-" + date + "'";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            output = retreiveData(resultSet);
        }
        catch(SQLException e) {}

        return output;
    }

    public String insertRecord(SaleRecord saleRecord) {

        if(statement == null)
            return "Ошибка - База не подключена - statement error";

        if(saleRecord == null)
            return "Ошибка - Объект записи не инициализирован";

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String saleDate = dateFormat.format(saleRecord.date);

        String values = "(" +
                Long.toString(saleRecord.userId) + ", '" +
                saleRecord.username + "', '" +
                saleRecord.message + "', '" +
                saleDate + "')";

        String query = "insert into " + tableName + " (userId, username, message, date) values " + values;
        try {
            int result = statement.executeUpdate(query);
        }
        catch(SQLException e) {
            return "statement.executeUpdate(query) - error";
        }
        return "Записал";
    }


}
