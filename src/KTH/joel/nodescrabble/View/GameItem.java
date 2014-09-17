package KTH.joel.nodescrabble.View;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 2013-12-26
 * Time: 13:16
 * To change this template use File | Settings | File Templates.
 */
public class GameItem
{
    private int sessionId;
    private int playerId;

    public GameItem(int sid)
    {
        sessionId = sid;
    }

    public GameItem(int sid, int pid)
    {
        sessionId = sid;
        playerId  = pid;
    }

    public int getSessionId()
    {
        return sessionId;
    }

    public int getPlayerId()
    {
        return playerId;
    }

    @Override
    public String toString()
    {
        return "Session " + sessionId;
    }
}
