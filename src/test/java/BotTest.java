import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class BotTest {

    private String resourcesPrefixPath = "src/test/resources/";

    @Test
    public void getDaySum_returnsValue() {
        Bot bot = new Bot();
        try {
            Assert.assertEquals(2001, bot.getDaySum(resourcesPrefixPath + "getDaySum_test_file.csv"), 0.0001);
        }
        catch(IOException e) {}
    }

    @Test
    public void getMonthSum_notEmptyFileList() {
        Bot bot = new Bot();
        Assert.assertEquals(10000, bot.getMonthSum(resourcesPrefixPath + "10-2017"), 0.001);
    }

    @Test
    public void getMonthSum_emptyFileList() {
        Bot bot = new Bot();
        Assert.assertEquals(0, bot.getMonthSum(resourcesPrefixPath + "09-2017"), 0.001);
    }

}
