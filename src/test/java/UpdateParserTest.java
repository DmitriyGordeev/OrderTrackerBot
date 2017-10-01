import org.junit.Assert;
import org.junit.Test;

public class UpdateParserTest {

    @Test
    public void parsePrice_returnsZero() {
        Assert.assertEquals(-1, UpdateParser.parsePrice(""), 0.0001);
        Assert.assertEquals(-1, UpdateParser.parsePrice("hello!"), 0.0001);
    }

    @Test
    public void parsePrice_returnsNumber() {

        Assert.assertEquals(0, UpdateParser.parsePrice("Hello 0"), 0.0001);
        Assert.assertEquals(100, UpdateParser.parsePrice("hello 100"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.parsePrice("hello 12.56"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.parsePrice("12.56"), 0.0001);
        Assert.assertEquals(12.56, UpdateParser.parsePrice("Some value 12.56 here!"), 0.0001);

    }

}
