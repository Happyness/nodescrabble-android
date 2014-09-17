package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.Scrabble;
import android.util.Log;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.EventCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessageListener extends MasterListener implements EventCallback
{
    private Scrabble activity;

    public ChatMessageListener(Scrabble s)
    {
        activity = s;
    }

    @Override
    public void onEvent(String event, JSONArray argument, Acknowledge acknowledge) {
        try {
            Log.d("SocketHandler", "Event:" + event + "Arguments:"
                    + argument.toString(2));

            JSONObject json = argument.getJSONObject(0);
            activity.updateChat(json.getInt("playerid"), json.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
