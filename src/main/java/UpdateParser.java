import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateParser {

    public static float parsePrice(String str) throws Exception {

        String regex = "цена:\\s*[0-9]+((\\.|,)[0-9]+)?";
        Matcher m = Pattern.compile(regex).matcher(str.toLowerCase());

        ArrayList<String> matches = new ArrayList<String>();
        while (m.find())
            matches.add(m.group(0));

        float value = 0;
        if(!matches.isEmpty()) {
            value = UpdateParser.findNumerics(matches.get(0));
        }
        else {
            throw new Exception("regex matches are not found");
        }

        return value;
    }

    public static float findNumerics(String str) throws Exception {

        String regex = "[0-9]+((\\.|,)[0-9]+)?";
        Matcher m = Pattern.compile(regex).matcher(str);

        ArrayList<String> matches = new ArrayList<String>();
        while (m.find())
            matches.add(m.group(0));

        if(!matches.isEmpty()) {
            String match = matches.get(0);
            return Float.parseFloat(match.replace(",", "."));
        }
        else {
            throw new Exception("matches are not found");
        }
    }

}
