package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.Scrabble;
import android.util.Log;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.EventCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 2013-12-26
 * Time: 12:29
 * To change this template use File | Settings | File Templates.
 */
public class GameStartListener extends MasterListener implements EventCallback
{
    private Scrabble activity;

    public GameStartListener(Scrabble s)
    {
        activity = s;
    }

    @Override
    public void onEvent(String event, JSONArray argument, Acknowledge acknowledge) {
        try {
            Log.d("SocketHandler", "Event:" + event + "Arguments:"
                    + argument.toString(2));

            JSONObject json = argument.getJSONObject(0);
            int turn = json.getInt("turn");
            JSONArray tiles = json.getJSONArray("tiles");
            JSONArray playedTiles = json.optJSONArray("playedTiles");
            int size = json.getInt("size");
            JSONArray players = json.optJSONArray("players");

            if (playedTiles == null) {
                playedTiles = new JSONArray();
            }

            activity.startGame(players, tiles, playedTiles, turn, size);
        } catch (JSONException e) {
            Log.d("json", e.getMessage());
        }

    }
}
