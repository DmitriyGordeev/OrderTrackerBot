import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.java2d.pipe.SpanShapeRenderer;

import javax.xml.bind.ParseConversionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class BotTest {

    private String resourcesPrefixPath = "src/test/resources/";
    private Bot bot;

    @Before
    public void setupBot() {
        bot = new Bot("sales_test");
    }


    @Test
    public void reformatDate_reformatting() {
        try {

            Assert.assertEquals("10-2017",
                    bot.reformateDate(
                            "22-10-2017",
                            "dd-MM-yyyy",
                            "MM-yyyy"));

            Assert.assertEquals("22.10.2017",
                    bot.reformateDate(
                            "22-10-2017",
                            "dd-MM-yyyy",
                            "dd.MM.yyyy"));

        }
        catch(ParseException e) {}

    }


    @Test
    public void getDaySum_db_test() throws Exception {
        Assert.assertEquals(0, bot.getDaySum_db(""), 0);
        Assert.assertEquals(0, bot.getDaySum_db("bad formatted string"), 0);
        Assert.assertEquals(11219 * 2, bot.getDaySum_db("10-10-2017"), 0);
    }

    @Test
    public void daysumCommand_db_test() {

        String response = bot.daysumCommand_db("/daysum 10-10-2017");
        Assert.assertTrue(response.contains("Выручка"));

        response = bot.daysumCommand_db("/daysum bad-formatting");
        Assert.assertTrue(response.contains("Неверный"));
    }


    @Test
    public void getMonthSum_db_test() throws Exception {
        Assert.assertEquals(0, bot.getMonthSum_db(""), 0);
        Assert.assertEquals(0, bot.getMonthSum_db("bad formatted string"), 0);
        Assert.assertEquals(0, bot.getMonthSum_db("10-10-2017"), 0);
        Assert.assertEquals(11219 * 2, bot.getMonthSum_db("10-2017"), 0);
    }

    @Test
    public void monthsumCommand_db_test() throws Exception {

        String response = bot.monthsumCommand_db("/monthsum 09-2017");
        Assert.assertTrue(response.contains("Выручка"));

        response = bot.monthsumCommand_db("/monthsum 02-09-2017");
        Assert.assertTrue(response.contains("Выручка"));
        System.out.println(response);

        response = bot.monthsumCommand_db("/monthsum bad-formatting");
        Assert.assertTrue(response.contains("Неверный"));
    }


    @Test
    public void createDayFile_testFileExists() throws SQLException, IOException {
        bot.createDayFile("02-09-2017");
        File f = new File("dayfile.csv");
        Assert.assertTrue(f.exists());
    }

    // TODO: create tests for


    @Test
    public void createMonthFile_testFileExists() throws SQLException, IOException {
        bot.createMonthFile("09-2017");
        File f = new File("monthfile.csv");
        Assert.assertTrue(f.exists());
    }


    // TODO: create tests for
    // Bot.prepareMonthForUpload() and Bot.monthFileCommand_db


}
