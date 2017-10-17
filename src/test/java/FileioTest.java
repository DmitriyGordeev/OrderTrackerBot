import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class FileioTest {

    @Test
    public void test_write_read_file() {
        String filename = "fileio-test.txt";
        String fileContent = "Message!\n";

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

    @Test
    public void correctEncoding() throws FileNotFoundException,
            UnsupportedEncodingException {

        String filename = "encoding-test.txt";

        PrintWriter writer = new PrintWriter(filename, "cp1251");
        writer.print("русские символы здесь!");
        writer.close();
    }

}
