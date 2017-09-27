import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FileioTest {

    @Test
    public void test_write_read_file() {
        String filename = "fileio-test.txt";
        String fileContent = "Message!";

        try {
            Fileio.writefile(filename, fileContent);
        }
        catch(IOException e) { e.printStackTrace(); }

        String content = "";
        try {
            content = Fileio.readfile(filename);
        }
        catch(IOException e) { e.printStackTrace(); }


        System.out.println("expected: " + fileContent);
        System.out.println("-----------------------------------");
        System.out.println("actual: " + content);

        Assert.assertEquals(fileContent, content);
    }

}
