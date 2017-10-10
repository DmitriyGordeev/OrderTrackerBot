import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaleRecord {

    int id;
    long userId;
    String username;
    String message;
    float value;
    Date date;

    public String csv(String dateformat) {

        DateFormat dateFormat = new SimpleDateFormat(dateformat);
        return Integer.toString(id) + ";"
                + Long.toString(userId) + ";"
                + username + ";"
                + message + ";"
                + Float.toString(value) + ";"
                + dateFormat.format(date);
    }
}
