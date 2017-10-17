import java.io.*;

public class Fileio {

    public static String readfile(String filename) throws IOException {

        String out = "";
        File f = new File(filename);

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String line;
        while((line = br.readLine()) != null) {
            out += line + "\n";
        }

        br.close();
        fr.close();

        return out;
    }

    public static void writefile(String filename, String content) throws IOException {

        PrintWriter writer = new PrintWriter(filename, "cp1251");
        writer.print(content);
        writer.close();
    }

}
