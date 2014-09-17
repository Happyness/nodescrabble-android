package KTH.joel.nodescrabble;

import KTH.joel.nodescrabble.Listener.*;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import com.koushikdutta.async.http.socketio.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SocketHandler
        implements ErrorCallback, JSONCallback, StringCallback, DisconnectCallback, ConnectCallback
{
    private SocketIOClient socket;
    private Scrabble mainActivity;

    public boolean isConnected()
    {
        return socket != null;
    }

    public SocketHandler(Scrabble main)
    {
        mainActivity = main;
    }

    public void connect(String address)
    {
        mainActivity.showLoader("Connecting to Heroku server ...");
        SocketIOClient.connect(address, SocketHandler.this, new Handler());
    }

    @Override
    public void onConnectCompleted(Exception ex, SocketIOClient client)
    {
        if (ex != null) {
            Log.d("SocketHandler", ex.getMessage());
            mainActivity.hideLoader("... connection failed");
            return;
        }

        mainActivity.hideLoader("... connection established");

        Log.d("SocketHandler", "Got successful socket.io connection!");

        //Save the returned SocketIOClient instance into a variable so you can disconnect it later
        client.setDisconnectCallback(SocketHandler.this);
        client.setErrorCallback(SocketHandler.this);
        client.setJSONCallback(SocketHandler.this);
        client.setStringCallback(SocketHandler.this);

        //You need to explicitly specify which events you are interested in receiving
        client.addListener("initgame-response", new InitResponseListener(mainActivity));
        client.addListener("update", new UpdateListener(mainActivity));
        client.addListener("joingame-response", new JoinResponseListener(mainActivity));
        client.addListener("game-started", new GameStartListener(mainActivity));
        client.addListener("playmove-response", new PlayResponseListener(mainActivity));
        client.addListener("game-ended", new EndGameListener(mainActivity));
        client.addListener("servermessage", new MessageListener(mainActivity));
        client.addListener("chatmessage", new ChatMessageListener(mainActivity));

        this.socket = client;

        GameSession session = mainActivity.getActiveSession();
        sendMessage("gameinfo", mainActivity.getGameInfo());

        if (session != null && session.getPlayerId() > 0 && session.getId() > 0) {
            sendMessage("joingame", session.asJsonString());
        }
    }

    public SocketIOClient getSocket()
    {
        return socket;
    }

    public void sendMessage(String type, JSONObject message)
    {
        if (isConnected()) {
            JSONArray json = new JSONArray();
            json.put(message);
            socket.emit(type, json);
        }
    }

    public void sendMessage(String type, String message)
    {
        if (isConnected()) {
            Log.d("json", String.format("Sending with type %s: %s", type, message));
            socket.emit(type, JsonHelper.getJsonFromString(message));
        }
    }

    @Override
    public void onString(String string, Acknowledge acknowledge) {
        Log.d("SocketHandler", string);
    }

    @Override
    public void onJSON(JSONObject json, Acknowledge acknowledge) {
        try {
            Log.d("SocketHandler", "json:" + json.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(String error) {
        Log.d("SocketHandler", error);
        mainActivity.flashMessage(error);
    }

    @Override
    public void onDisconnect(Exception e)
    {
        mainActivity.flashMessage("Server disconnected");
        mainActivity.switchToLayout(Scrabble.LAYOUTS.CHOOSEGAME);
        Log.d("SocketHandler", "Disconnected:" + e.getMessage());
    }
}
