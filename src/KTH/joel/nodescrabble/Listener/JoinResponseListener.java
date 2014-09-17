package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.GameSession;
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
public class JoinResponseListener extends MasterListener implements EventCallback
{
    private Scrabble activity;

    public JoinResponseListener(Scrabble s)
    {
        activity = s;
    }

    @Override
    public void onEvent(String event, JSONArray argument, Acknowledge acknowledge) {
        try {
            Log.d("SocketHandler", "Event:" + event + "Arguments:"
                    + argument.toString(2));

            JSONObject json = argument.getJSONObject(0);
            String type = json.getString("result");

            if (type.equals("success")) {
                Log.d("json", "Got player: " + json.toString());
                GameSession session = activity.getActiveSession();
                session.setPlayerId(json.getInt("playerid"));
                activity.sendReady(session);
            } else {
                activity.flashMessage(json.getString("message"));

                if (activity.isLayoutState(Scrabble.LAYOUTS.INGAME)) {
                    activity.switchToLayout(Scrabble.LAYOUTS.CHOOSEGAME);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
