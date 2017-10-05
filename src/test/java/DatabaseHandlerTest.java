import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandlerTest {

    private DatabaseHandler databaseHandler;

    @Before
    public void setupConnection() {

        //TODO: remove hardcoded values from here:
        String url = "jdbc:mysql://mysql5.gear.host:3306/orderbot";
        String user = "orderbot";
        String pass = "Cc2-_M6KqMWH";

        databaseHandler = new DatabaseHandler(url, user, pass, "sales_test");
    }

    @Test
    public void getRecordsNonEmpty() throws SQLException {
        ArrayList<SaleRecord> records = databaseHandler.getRecords();
        for(SaleRecord sr : records) {
            System.out.println(sr.csv("dd-MM-yyyy"));
        }
    }



    @Test(expected = SQLException.class)
    public void retreiveData_throwsException() throws SQLException {

        Connection connection = databaseHandler.getConnection();
        Statement  statement  = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT (id) FROM " + databaseHandler.getTableName());

        // this method should throw and SQLException
        // then test will be passed
        ArrayList<SaleRecord> output = databaseHandler.retreiveData(resultSet);

        resultSet.close();
        databaseHandler.close();
    }

    @Test
    public void retreiveData_notThrowned() throws SQLException {

        Connection connection = databaseHandler.getConnection();
        Statement  statement  = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM " + databaseHandler.getTableName());
        ArrayList<SaleRecord> output = databaseHandler.retreiveData(resultSet);

        resultSet.close();
        databaseHandler.close();
    }

    @Test
    public void retreiveData_daySpecified() throws SQLException {
        ArrayList<SaleRecord> output = databaseHandler.getRecords("02-09-2017");
        for(SaleRecord sr : output) {
            System.out.println(sr.csv("dd-MM-yyyy"));
        }
    }

    @Test
    public void retreiveData_daySpecified_isEmpty_badString() throws SQLException {
        ArrayList<SaleRecord> output = databaseHandler.getRecords("bad string");
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void retreiveData_daySpecified_isEmpty_emptyString() throws SQLException {
        ArrayList<SaleRecord> output = databaseHandler.getRecords("");
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void retreiveData_monthSpecified() throws SQLException {
        ArrayList<SaleRecord> output = databaseHandler.getRecordsMonth("09-2017");
        for(SaleRecord sr : output) {
            System.out.println(sr.csv("dd-MM-yyyy"));
        }
        Assert.assertFalse(output.isEmpty());
    }

    @Test
    public void retreiveData_monthSpecified_isEmpty_badString() throws SQLException {
        ArrayList<SaleRecord> output = databaseHandler.getRecordsMonth("badString");
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void retreiveData_monthSpecified_isEmpty_emptyString() throws SQLException {
        ArrayList<SaleRecord> output = databaseHandler.getRecordsMonth("");
        Assert.assertTrue(output.isEmpty());
    }


    @Test
    public void insertRecord_returnsTrue() throws SQLException {

        SaleRecord saleRecord = new SaleRecord();
        saleRecord.id = 0;
        saleRecord.userId = 13233;
        saleRecord.username = "UnitTesting Username";
        saleRecord.message = "UnitTestMessage";
        saleRecord.date = new Date();

        Assert.assertEquals("Записал", databaseHandler.insertRecord(saleRecord));
    }

}
