import org.junit.Assert;
import org.junit.Test;

public class FileioTest {

    @Test
    public void test_write_read_file() {
        String filename = "fileio-test.txt";
        String fileContent = "Message!";

        Fileio.writefile(filename, fileContent);
        String content = Fileio.readfile(filename);

        System.out.println("expected: " + fileContent);
        System.out.println("-----------------------------------");
        System.out.println("actual: " + content);

        Assert.assertEquals(fileContent, content);
    }

}
