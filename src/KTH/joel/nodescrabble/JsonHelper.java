package KTH.joel.nodescrabble;

import KTH.joel.nodescrabble.View.GameItem;
import KTH.joel.nodescrabble.View.LanguageItem;
import KTH.joel.nodescrabble.View.Tile;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 2013-12-27
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class JsonHelper
{
    public static JSONArray getJsonFromString(String data)
    {
        JSONArray jsonArray = new JSONArray();
        try {
            JSONObject json = new JSONObject(data);
            jsonArray.put(json);
        } catch (JSONException e) {
            jsonArray.put("");
        }

        return jsonArray;
    }

    public static Tile convertJsonTile(JSONObject tile)
    {
        try {
            String letter = tile.getString("letter");
            char value    = letter.isEmpty() ? ' ' : letter.charAt(0);
            int score   = tile.getInt("score");
            return new Tile(value, score);
        } catch (JSONException e) {
            Log.d("json", e.getMessage());
        }

        return null;
    }

    public static <T> ArrayList<T> jsonArrayAsList(JSONArray json, Class<T> type)
    {
        ArrayList<T> list = new ArrayList<T>();

        try {
            for (int i = 0; i < json.length(); i++) {
                if (type == GameSession.class) {
                    JSONObject game = json.getJSONObject(i);
                    int sid = game.getInt("sessionid");

                    list.add((T)new GameSession(sid));
                }

                if (type == String.class) {
                    list.add((T)json.getString(i));
                }

                if (type == Tile.class) {
                    list.add((T)convertJsonTile(json.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            Log.d("json", e.getMessage());
        }

        return list;
    }
}
