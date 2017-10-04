import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseHandlerTest {

    private DatabaseHandler databaseHandler;

    @Before
    public void setupConnection() {

        //TODO: remove hardcoded values from here:
        String url = "jdbc:mysql://mysql5.gear.host:3306/orderbot";
        String user = "orderbot";
        String pass = "Cc2-_M6KqMWH";

        databaseHandler = new DatabaseHandler();
        Assert.assertEquals(true, databaseHandler.connect(url, user, pass));
    }

    @Test
    public void getRecordsNonEmpty() {
        ArrayList<SaleRecord> records = databaseHandler.getRecords();
        for(SaleRecord sr : records) {
            System.out.println(sr.csv("dd-MM-yyyy"));
        }
    }


    @Test(expected = SQLException.class)
    public void retreiveData_throwsException() throws SQLException {

        Statement statement = databaseHandler.getStatement();
        Assert.assertFalse(statement.equals(null));

        ResultSet resultSet = statement.executeQuery("SELECT (id) FROM sales");
        ArrayList<SaleRecord> output = databaseHandler.retreiveData(resultSet);
    }


    @Test
    public void retreiveData_notThrowned() throws SQLException {

        Statement statement = databaseHandler.getStatement();
        Assert.assertFalse(statement.equals(null));

        ResultSet resultSet = statement.executeQuery("SELECT * FROM sales");
        ArrayList<SaleRecord> output = databaseHandler.retreiveData(resultSet);

    }



    @Test
    public void retreiveData_daySpecified() throws SQLException {

        Statement statement = databaseHandler.getStatement();
        Assert.assertFalse(statement.equals(null));

        ArrayList<SaleRecord> output = databaseHandler.getRecords("02-09-2017");
        for(SaleRecord sr : output) {
            System.out.println(sr.csv("dd-MM-yyyy"));
        }

    }

    @Test
    public void retreiveData_daySpecified_isEmpty_badString() throws SQLException {

        Statement statement = databaseHandler.getStatement();
        Assert.assertFalse(statement.equals(null));

        ArrayList<SaleRecord> output = databaseHandler.getRecords("bad string");
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void retreiveData_daySpecified_isEmpty_emptyString() throws SQLException {

        Statement statement = databaseHandler.getStatement();
        Assert.assertFalse(statement.equals(null));

        ArrayList<SaleRecord> output = databaseHandler.getRecords("");
        Assert.assertTrue(output.isEmpty());
    }



    @Test
    public void retreiveData_monthSpecified() throws SQLException {

        Statement statement = databaseHandler.getStatement();
        Assert.assertFalse(statement.equals(null));

        ArrayList<SaleRecord> output = databaseHandler.getRecordsMonth("09-2017");
        for(SaleRecord sr : output) {
            System.out.println(sr.csv("dd-MM-yyyy"));
        }

    }

    @Test
    public void retreiveData_monthSpecified_isEmpty_badString() throws SQLException {

        Statement statement = databaseHandler.getStatement();
        Assert.assertFalse(statement.equals(null));

        ArrayList<SaleRecord> output = databaseHandler.getRecordsMonth("badString");
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void retreiveData_monthSpecified_isEmpty_emptyString() throws SQLException {

        Statement statement = databaseHandler.getStatement();
        Assert.assertFalse(statement.equals(null));

        ArrayList<SaleRecord> output = databaseHandler.getRecordsMonth("");
        Assert.assertTrue(output.isEmpty());
    }


}
