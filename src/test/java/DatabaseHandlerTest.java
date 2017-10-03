import org.junit.Assert;
import org.junit.Test;

public class DatabaseHandlerTest {

    @Test
    public void test_mysqlConnection() {

        //TODO: remove hardcoded values from here:
        String url = "jdbc:mysql://mysql5.gear.host:3306/orderbot";
        String user = "orderbot";
        String pass = "Cc2-_M6KqMWH";

        DatabaseHandler db = new DatabaseHandler();
        Assert.assertEquals(true, db.connect(url, user, pass));
    }
}
