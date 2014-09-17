package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.GameSession;
import KTH.joel.nodescrabble.JsonHelper;
import KTH.joel.nodescrabble.Scrabble;
import KTH.joel.nodescrabble.View.GameItem;
import KTH.joel.nodescrabble.View.LanguageItem;
import KTH.joel.nodescrabble.View.Tile;
import android.util.Log;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.EventCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 2013-12-26
 * Time: 12:29
 * To change this template use File | Settings | File Templates.
 */
public class UpdateListener extends MasterListener implements EventCallback
{
    private Scrabble activity;

    public UpdateListener(Scrabble s)
    {
        activity = s;
    }

    @Override
    public void onEvent(String event, JSONArray argument, Acknowledge acknowledge) {
        try {
            Log.d("SocketHandler", "Event: " + event + ", Arguments: "
                    + argument.toString(2));

            JSONObject json = argument.getJSONObject(0);
            String type = json.getString("type");
            GameSession session = activity.getActiveSession();

            if (type.equals("gameinfo")) {
                 Log.d("json", "Got gameinfo: " + json.toString());

                 ArrayList<GameSession> games = JsonHelper.jsonArrayAsList(json.optJSONArray("games"), GameSession.class);
                 ArrayList<String> languages = JsonHelper.jsonArrayAsList(json.optJSONArray("languages"), String.class);

                 if (games != null) activity.updateGameList(games);
                 if (languages != null) activity.updateLanguageList(languages);
            } else if (type.equals("playable-tiles")) {
                activity.getGameBoard().updateTileHolder(JsonHelper.jsonArrayAsList(json.optJSONArray("tiles"), Tile.class));
            } else if (type.equals("move")) {
                JSONArray tiles = json.optJSONArray("tiles");
                int score       = json.optInt("score");
                int turn        = json.optInt("turn");
                int playerid    = json.optInt("playerid");

                session.setTurn(turn);

                if (tiles != null) {
                    session.addTilesToBoard(tiles);
                    if (playerid != session.getPlayerId()) {
                        activity.getGameBoard().discardTiles();
                    }
                    activity.getGameBoard().updateBoard(session.getBoard());
                }

                if (score >= 0 && playerid > 0) {
                    activity.updateScoreBoard(score, playerid);
                }

                activity.flashMessage(String.format("It is now %s!", session.turnAsString()), false);
            }
        } catch (JSONException e) {
            Log.d("json", e.toString());
        }
    }
}
