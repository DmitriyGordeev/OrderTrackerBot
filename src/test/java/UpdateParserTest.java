import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateParserTest {

    @Test
    public void parsePrice_returnsZero() throws Exception {
        Assert.assertEquals(0, UpdateParser.findNumerics(""), 0.0001);
        Assert.assertEquals(0, UpdateParser.findNumerics("hello!"), 0.0001);
    }

    @Test
    public void parsePrice_returnsNumber_dot() throws Exception {
        Assert.assertEquals(0, UpdateParser.findNumerics("Hello 0"), 0.0001);
        Assert.assertEquals(100, UpdateParser.findNumerics("hello 100"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.findNumerics("hello 12.56"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.findNumerics("12.56"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.findNumerics("Some value 12.56 here!"), 0.0001);
    }

    @Test
    public void parsePrice_returnsNumber_comma() throws Exception {
        Assert.assertEquals(100, UpdateParser.findNumerics("hello 100"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.findNumerics("hello 12,56"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.findNumerics("12,56"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.findNumerics("Some value 12,56 here!"), 0.0001);
    }

    @Test
    public void parsePriceFromExpRequest_option1() throws Exception {

        String request = "Игрушка PowerMan 5000 цена: 500";
        float value = UpdateParser.parsePrice(request);
        Assert.assertEquals(500, value, 0);
    }

    @Test
    public void parsePriceFromExpRequest_option2() throws Exception {

        String request = "Игрушка PowerMan 5000 цена:500";
        float value = UpdateParser.parsePrice(request);
        Assert.assertEquals(500, value, 0);
    }

    @Test(expected = Exception.class)
    public void parsePriceFromExpRequest_option3() throws Exception {

        String request = "Игрушка PowerMan 5000 ";
        float value = UpdateParser.parsePrice(request);
        Assert.assertEquals(0, value, 0);
    }

    @Test(expected = Exception.class)
    public void parsePriceFromExpRequest_option4() throws Exception {

        String request = "Игрушка PowerMan 5000 цена:";
        float value = UpdateParser.parsePrice(request);
        Assert.assertEquals(0, value, 0);
    }

    @Test(expected = Exception.class)
    public void parsePriceFromExpRequest_option5() throws Exception {

        String request = "Игрушка PowerMan 5000 цена: ахтунг";
        float value = UpdateParser.parsePrice(request);
        Assert.assertEquals(0, value, 0);
    }


}
