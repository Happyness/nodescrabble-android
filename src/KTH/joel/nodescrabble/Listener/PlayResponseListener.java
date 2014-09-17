package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.GameSession;
import KTH.joel.nodescrabble.JsonHelper;
import KTH.joel.nodescrabble.Scrabble;
import KTH.joel.nodescrabble.View.Tile;
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
public class PlayResponseListener extends MasterListener implements EventCallback
{
    private Scrabble activity;

    public PlayResponseListener(Scrabble s)
    {
        activity = s;
    }

    private String wordsAsString(JSONArray json)
    {
        String words = "";
        try {
            for (int i = 0; i < json.length(); i++) {
                if (i != 0) {
                    words += ", ";
                }
                words += json.getString(i);
            }
        } catch (JSONException e) {
            Log.d("json", e.toString());
        }

        return words;
    }

    @Override
    public void onEvent(String event, JSONArray argument, Acknowledge acknowledge) {
        try {
            Log.d("SocketHandler", "Event:" + event + "Arguments:"
                    + argument.toString(2));

            JSONObject json = argument.getJSONObject(0);
            String type = json.getString("result");

            if (type.equals("success")) {
                GameSession session = activity.getActiveSession();
                JSONArray newtiles = json.optJSONArray("newtiles");
                int totalscore  = json.optInt("totalscore");
                int score       = json.optInt("score");
                int turn        = json.optInt("turn");
                int playerid    = json.optInt("playerid");
                JSONArray words = json.optJSONArray("words");

                session.setTurn(turn);

                if (newtiles != null && activity.isLayoutState(Scrabble.LAYOUTS.INGAME)) {
                    activity.getGameBoard().removeTiles(Tile.STATE.SWAP);
                    activity.getGameBoard().updateMoved();
                    activity.getGameBoard().updateTileHolder(JsonHelper.jsonArrayAsList(newtiles, Tile.class));
                }

                if (totalscore >= 0 && playerid > 0) {
                    activity.flashMessage(String.format("You got %d points with words %s", score, wordsAsString(words)));
                    activity.updateScoreBoard(totalscore, playerid);
                }

                activity.flashMessage(String.format("It is now %s!", session.turnAsString()), false);
            } else {
                //activity.getGameBoard().discardTiles();
                activity.flashMessage(json.getString("message"));
            }
        } catch (JSONException e) {
            Log.d("json", e.toString());
        }

    }
}
