package KTH.joel.nodescrabble;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SessionHandler
{
    private ArrayList<GameSession> sessions = new ArrayList<GameSession>();
    private GameSession activeSession;

    public SessionHandler()
    {
    }

    public SessionHandler(String data)
    {
        loadSessions(data);
    }

    public GameSession getActiveSession()
    {
        return activeSession;
    }

    public ArrayList<GameSession> getSessions()
    {
        return sessions;
    }

    public void setActiveSession(GameSession session)
    {
        addSession(session);
        this.activeSession = session;
    }

    private boolean exists(GameSession session)
    {
        for (GameSession s: sessions) {
            if (session.getId() == s.getId()) return true;
        }
        return false;
    }

    public void addSession(GameSession session)
    {
        if (!exists(session)) sessions.add(session);
    }

    public void removeSession(GameSession session)
    {
        sessions.remove(session);
    }

    private JSONObject sessionAsJSONObject(GameSession session, boolean active) throws JSONException
    {
        JSONObject object = new JSONObject();
        object.put("id", session.getId());
        object.put("playerid", session.getPlayerId());
        object.put("active", active);
        return object;
    }

    public String sessionsAsJsonString()
    {
        try {
            JSONObject object = new JSONObject();
            JSONArray sessionArray = new JSONArray();
            for (GameSession s: sessions) {
                sessionArray.put(sessionAsJSONObject(s, false));
            }

            if (activeSession != null) sessionArray.put(sessionAsJSONObject(activeSession, true));

            object.put("sessions", sessionArray);
            return object.toString();
        } catch (JSONException e) {
            Log.d("json", e.toString());
            return null;
        }
    }

    public boolean loadSessions(JSONArray json)
    {
        try {
            for (int i = 0; i < json.length(); i++) {
                JSONObject object = json.getJSONObject(i);
                GameSession session = new GameSession(object.getInt("id"), object.getInt("playerid"));

                boolean active = object.getBoolean("active");
                if (active) {
                    activeSession = session;
                } else {
                    addSession(session);
                }
            }
            return true;
        } catch (JSONException e) {
            Log.d("json", e.toString());
            return false;
        }
    }

    public boolean loadSessions(String data)
    {
        try {
            JSONObject object = new JSONObject(data);
            JSONArray json    = object.getJSONArray("sessions");
            return loadSessions(json);
        } catch (JSONException e) {
            Log.d("json", e.toString());
            return false;
        }
    }
}
