import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler {

    private String url;
    private String user;
    private String password;
    private String tablename;

    private Connection connection;
    private Statement statement;

    public DatabaseHandler(String url,
                           String user,
                           String password,
                           String tablename) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.tablename = tablename;

        connection = null;
        statement  = null;
    }


    public void connect() throws SQLException {
        connection = DriverManager.getConnection(url, user, password);
        statement  = connection.createStatement();
    }
    public void close() throws SQLException {

        statement.close();

        if(!connection.isClosed())
            connection.close();
    }


    public void setTableName(String tablename) { this.tablename = tablename; }
    public String getTableName() {
        return tablename;
    }
    public Connection getConnection() { return connection; }


    public ArrayList<SaleRecord> retreiveData(ResultSet resultSet) throws SQLException {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        while(resultSet.next()) {
            SaleRecord saleRecord = new SaleRecord();
            saleRecord.id = resultSet.getInt("id");
            saleRecord.userId = resultSet.getInt("userId");
            saleRecord.username = resultSet.getString("username");
            saleRecord.message = resultSet.getString("message");

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            try {
                saleRecord.date = dateFormat.parse(resultSet.getString("date"));
            } catch (ParseException e) {
                continue;
            }
            output.add(saleRecord);
        }

        return output;
    }

    public ArrayList<SaleRecord> getRecords() throws SQLException {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        connect();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tablename);
        output = retreiveData(resultSet);

        resultSet.close();
        close();

        return output;
    }

    public ArrayList<SaleRecord> getRecords(String date) throws SQLException {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        connect();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tablename + " where date='" + date + "'");
        output = retreiveData(resultSet);

        resultSet.close();
        close();

        return output;
    }

    public ArrayList<SaleRecord> getRecordsMonth(String date) throws SQLException {

        ArrayList<SaleRecord> output = new ArrayList<SaleRecord>();
        String query = "SELECT * FROM " + tablename + " where date like '%-" + date + "'";

        connect();

        ResultSet resultSet = statement.executeQuery(query);
        output = retreiveData(resultSet);

        resultSet.close();
        close();

        return output;
    }

    public String insertRecord(SaleRecord saleRecord) throws SQLException {

        connect();

        if(statement == null)
            return "Ошибка - База не подключена - statement error";

        if(saleRecord == null)
            return "Ошибка - Объект записи не инициализирован";

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z");
        String saleDate = dateFormat.format(saleRecord.date);

        String values = "(" +
                Long.toString(saleRecord.userId) + ", '" +
                saleRecord.username + "', '" +
                saleRecord.message + "', '" +
                saleDate + "')";

        String query = "insert into " + tablename + " (userId, username, message, date) values " + values;
        int result = statement.executeUpdate(query);

        close();

        return "Записал";
    }


}
