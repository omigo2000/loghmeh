package loghmeh.utilities;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class Utils {
    public static ArrayList<String> decodeJsonStrToList(String jsonStr){
        ArrayList<String> restaurantsString = new ArrayList<String>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(jsonStr);
            JSONArray array = (JSONArray) obj;
            for(int i=0; i<array.size(); i++)
                restaurantsString.add(array.get(i).toString());
        }
        catch(ParseException e) {e.printStackTrace(); }
        return restaurantsString;
    }
}
