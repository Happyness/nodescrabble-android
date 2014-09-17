package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.R;
import KTH.joel.nodescrabble.Scrabble;
import KTH.joel.nodescrabble.View.GameBoard;
import KTH.joel.nodescrabble.View.Tile;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InGameClickListener implements View.OnClickListener
{
    private Scrabble activity;

    public InGameClickListener(Scrabble activity)
    {
        this.activity = activity;
    }

    private JSONArray createTileArray(ArrayList<Tile> tiles)
    {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Tile tile: tiles) {
                JSONObject jsonTile = new JSONObject();

                jsonTile.put("letter", Character.toString(tile.getLetter()));
                jsonTile.put("x", tile.getGridX() + 1);
                jsonTile.put("y", tile.getGridY() + 1);
                jsonArray.put(jsonTile);
            }
        } catch (JSONException e) {
            Log.d("json", e.getMessage());
        }

        return jsonArray;
    }

    private void changeBackground(View v, int id)
    {
        Drawable bg = activity.getResources().getDrawable(id);

        if (Build.VERSION.SDK_INT >= 16) {
            v.setBackground(bg);
        } else {
            v.setBackgroundDrawable(bg);
        }
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();

        if (id == R.id.backButton) {
            activity.switchToLayout(Scrabble.LAYOUTS.CHOOSEGAME);
            activity.updateGameList(activity.getSessionHandler().getSessions());
            return;
        } else {
            if (activity.getActiveSession() == null) {
                activity.flashMessage("No active session available");
                return;
            }
            if (!activity.getActiveSession().isMyTurn() && id != R.id.shuffle && id != R.id.clearMoved) {
                activity.flashMessage("It is not your turn");
                return;
            }
        }

        boolean send = false;
        JSONObject json = new JSONObject();

        try {
            json.put("playerid", activity.getActiveSession().getPlayerId());
            json.put("sessionid", activity.getActiveSession().getId());
            GameBoard board = activity.getGameBoard();

            switch (id) {
                case R.id.clearMoved :
                    board.discardTiles();
                    break;
                case R.id.shuffle :
                    board.shuffleTiles();
                    break;
                case R.id.playMove :
                    ArrayList<Tile> move = board.getTiles(Tile.STATE.MOVED);
                    if (move.size() == 0) {
                        activity.flashMessage("No tiles are moved to board");
                        Log.d("gameplay", "Couldnt find any tiles played");
                        return;
                    }
                    json.put("move", createTileArray(move));
                    send = true;
                    break;
                case R.id.passMove:
                    board.discardTiles();
                    json.put("move", "pass");
                    send = true;
                    break;
                case R.id.swapMove:
                    if (board.isSwapMode()) {
                        json.put("move", "swap");
                        ArrayList<Tile> tiles = board.getTiles(Tile.STATE.SWAP);
                        board.setSwapMode(false);
                        changeBackground(v, R.drawable.button);

                        if (tiles.size() > 0) {
                            json.put("tiles", createTileArray(tiles));
                            send = true;
                        }
                    } else {
                         activity.flashMessage("You are now in swap mode, click on tiles to swap and when done, press swap button again to swap.");
                         changeBackground(v, R.drawable.selected_button);
                         board.setSwapMode(true);
                    }
                    break;
            }
        } catch (JSONException e) {
             Log.d("json", e.getMessage());
        }

        if (send) activity.getSocketHandler().sendMessage("playmove", json.toString());
    }
}
