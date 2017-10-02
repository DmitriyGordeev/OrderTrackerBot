import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class BotTest {

    @Test
    public void getDaySum_returnsValue() {
        Bot bot = new Bot();
        try {
            Assert.assertEquals(2001, bot.getDaySum("src/test/resources/getDaySum_test_file.csv"), 0.0001);
        }
        catch(IOException e) {}
    }

}
