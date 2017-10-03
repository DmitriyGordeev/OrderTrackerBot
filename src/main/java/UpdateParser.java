import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateParser {

    public static float parsePrice(String str) {

        String regex = "[0-9]+(\\.[0-9]+)?";
        Matcher m = Pattern.compile(regex).matcher(str);

        ArrayList<String> matches = new ArrayList<String>();
        while (m.find())
            matches.add(m.group(0));

        if(!matches.isEmpty())
        {
            try {
                return Float.parseFloat(matches.get(0));
            }
            catch(NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

}
