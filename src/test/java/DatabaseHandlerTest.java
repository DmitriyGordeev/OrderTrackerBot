import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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


}
