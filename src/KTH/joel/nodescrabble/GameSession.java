package KTH.joel.nodescrabble;

import KTH.joel.nodescrabble.View.Player;
import KTH.joel.nodescrabble.View.Tile;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class GameSession
{
    private int id;
    private int playerId;
    private int turn;
    private Tile[][] board;
    private ArrayList<Tile> unPlayedTiles = new ArrayList<Tile>();
    private ArrayList<Player> players = new ArrayList<Player>();

    public GameSession(int sid)
    {
        id = sid;
    }

    public GameSession(int sid, int pid)
    {
        id = sid;
        playerId = pid;
    }

    public void initBoard(int size)
    {
        board = new Tile[size][size];
    }

    public boolean isMyTurn()
    {
         return getPlayerId() == turn;
    }

    public String turnAsString()
    {
        return isMyTurn() ? "your turn" : "opponents turn";
    }

    public void printBoard()
    {
        Log.d("game", TextUtils.join(", ", board));
    }

    public void printTiles()
    {
        Log.d("game", TextUtils.join(", ", unPlayedTiles.toArray()));
    }

    public ArrayList<Tile> getUnPlayedTiles()
    {
        return unPlayedTiles;
    }

    public Tile[][] getBoard()
    {
        return board;
    }

    public void setTurn(int turn)
    {
        this.turn = turn;
    }

    public boolean isEmpty(int y, int x)
    {
        return (board[y][x] == null || (board[y][x].getLetter() == ' '));
    }

    public void setTile(int y, int x, Tile t)
    {
        //if (isEmpty(y, x)) {
            board[y][x] = t;
        //}
    }

    public void updateBoard(JSONArray board)
    {
        for (int y = 0; y < board.length(); y++) {
            try {
                JSONArray row = board.getJSONArray(y);

                for (int x = 0; x < row.length(); x++) {
                    String letter = row.optString(x);

                    if (!letter.isEmpty() && !letter.equals("null")) {
                        char value    = letter.charAt(0);
                        setTile(y, x, new Tile(value));
                    }
                }
            } catch (JSONException e) {
                Log.d("json", e.getMessage());
            }
        }
    }

    public void addTilesToBoard(JSONArray tiles)
    {
        try {
            for (int i = 0; i < tiles.length(); i++) {
                JSONObject tile = tiles.getJSONObject(i);
                String letter = tile.getString("letter");

                if (!letter.isEmpty()) {
                    char value    = letter.charAt(0);
                    int score     = tile.getInt("score");
                    int x         = tile.getInt("x");
                    int y         = tile.getInt("y");

                    //Log.d("gameplay", String.format("Try to add unplayed tile %c, [%d, %d]", value, x, y));

                    setTile(y-1, x-1, new Tile(value, score, x, y));
                }
            }
        } catch (JSONException e) {
            Log.d("json", e.getMessage());
        }
    }

    public void removeUnplayedTiles(ArrayList<Tile> tiles)
    {
        Iterator<Tile> it = unPlayedTiles.iterator();
        while (it.hasNext())
        {
            Tile t1 = it.next();

            for (Tile t2: tiles) {
                if (t1.getX() == t2.getX() && t1.getY() == t2.getY()) it.remove();
            }
        }
    }

    public void addUnplayedTiles(JSONArray tiles)
    {
        for (int i = 0; i < tiles.length(); i++) {
            try {
                if (this.unPlayedTiles.size() < 7) {
                JSONObject tile = tiles.getJSONObject(i);
                String letter = tile.getString("letter");
                char value    = letter.isEmpty() ? ' ' : letter.charAt(0);
                int score   = tile.getInt("score");
                Tile tileO  = new Tile(value, score);
                this.unPlayedTiles.add(tileO);
                }
            } catch (JSONException e) {
               Log.d("json", e.toString());
            }
        }
    }

    public String asJsonString()
    {
        if (id > 0 && playerId > 0) {
            return String.format("{sessionid: %d, playerid: %d}", id, playerId);
        } else if (id > 0) {
            return String.format("{sessionid: %d}", id);
        } else {
            return "{}";
        }
    }

    public ArrayList<Player> getPlayers()
    {
        return players;
    }

    public String getPlayerLabel(int id)
    {
        return getPlayerId() == id ? "You" : "Opponent";
    }

    public void addPlayer(Player p)
    {
        players.add(p);
    }

    public void setPlayerId(int id)
    {
        playerId = id;
    }

    public void setId(int sid)
    {
        id = sid;
    }

    public int getPlayerId()
    {
        return playerId;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "Session " + id + "pid: " + playerId;
    }
}
