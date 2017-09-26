import java.io.*;

public class Fileio {

    public static String readfile(String filename) {

        String out = "";
        File f = new File(filename);
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while((line = br.readLine()) != null) {
                out += line;
            }

            br.close();
            fr.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    public static void writefile(String filename, String content) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.print(content);
            writer.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

}
