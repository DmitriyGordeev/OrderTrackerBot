import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.ParseConversionEvent;
import java.io.IOException;
import java.text.ParseException;

public class BotTest {

    private String resourcesPrefixPath = "src/test/resources/";

    @Test
    public void getDaySum_returnsValue() throws Exception {
        Bot bot = new Bot();
        try {
            Assert.assertEquals(2001, bot.getDaySum(resourcesPrefixPath + "getDaySum_test_file.csv"), 0.0001);
        }
        catch(IOException e) {}
    }

    @Test
    public void getMonthSum_notEmptyFileList() throws Exception {
        Bot bot = new Bot();
        Assert.assertEquals(10000, bot.getMonthSum(resourcesPrefixPath + "10-2017"), 0.001);
    }

    @Test
    public void getMonthSum_emptyFileList() throws Exception {
        Bot bot = new Bot();
        Assert.assertEquals(0, bot.getMonthSum(resourcesPrefixPath + "09-2017"), 0.001);
    }

    @Test
    public void reformatDate_reformatting() throws Exception {
        Bot bot = new Bot();
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


    /* -------------------------------------------------------- */
    /* Updated methods with database: */


    @Test
    public void getDaySum_db_test() throws Exception {
        Bot bot = new Bot();
        Assert.assertEquals(0, bot.getDaySum_db(""), 0);
        Assert.assertEquals(0, bot.getDaySum_db("bad formatted string"), 0);
        Assert.assertEquals(5000, bot.getDaySum_db("02-09-2017"), 0);
    }

    @Test
    public void daysumCommand_db_test() throws Exception {
        Bot bot = new Bot();

        String response = bot.daysumCommand_db("/daysum 02-09-2017");
        Assert.assertTrue(response.contains("Выручка"));

        response = bot.daysumCommand_db("/daysum bad-formatting");
        Assert.assertTrue(response.contains("Неверный"));
    }


    @Test
    public void getMonthSum_db_test() throws Exception {
        Bot bot = new Bot();
        Assert.assertEquals(0, bot.getMonthSum_db(""), 0);
        Assert.assertEquals(0, bot.getMonthSum_db("bad formatted string"), 0);
        Assert.assertEquals(0, bot.getMonthSum_db("22-09-2017"), 0);
        Assert.assertEquals(15000, bot.getMonthSum_db("09-2017"), 0);
    }

    @Test
    public void monthsumCommand_db_test() throws Exception {
        Bot bot = new Bot();

        String response = bot.monthsumCommand_db("/monthsum 09-2017");
        Assert.assertTrue(response.contains("Выручка"));

        response = bot.monthsumCommand_db("/monthsum 02-09-2017");
        Assert.assertTrue(response.contains("Выручка"));
        System.out.println(response);

        response = bot.monthsumCommand_db("/monthsum bad-formatting");
        Assert.assertTrue(response.contains("Неверный"));
    }






}
