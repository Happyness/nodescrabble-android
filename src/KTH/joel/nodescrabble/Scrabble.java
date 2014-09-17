package KTH.joel.nodescrabble;

import KTH.joel.nodescrabble.Listener.ChooseGameClickListener;
import KTH.joel.nodescrabble.Listener.DragViewListener;
import KTH.joel.nodescrabble.View.*;
import KTH.joel.nodescrabble.Listener.InGameClickListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author          Joel Denke and Mathias Westman
 * @description     Front controller and main activity of the app
 */
public class Scrabble extends Activity
{
    public enum LAYOUTS
    {
        INGAME, CHOOSEGAME
    }

    private static final int RESULT_SETTINGS = 10;
    private GameBoard board;
    private LinearLayout scoreBoard;
    private SessionHandler sessionHandler;
    private SocketHandler socketHandler;
    private LAYOUTS layoutState = LAYOUTS.CHOOSEGAME;
    private ViewSwitcher switcher;
    private View ingameLayout;
    private View choosegameLayout;
    private  ArrayAdapter<GameSession> gameList;
    private  ArrayAdapter<String> languageList;
    private boolean init = false;
    private TextView responseView;
    private Chat chat;
    private final String serverAddress = "http://nodescrabbler.herokuapp.com";
    private ProgressDialog progressDialog;

    private static final String SESSION_DATA = "game_sessions";
    private static final String LAYOUT_STATE = "layout_state";
    private static final String CHAT_DATA = "game_chat";

    private BroadcastReceiver receiver;

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Create new instance of content
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        createLayouts();
        socketHandler = new SocketHandler(this);
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                checkNetwork();
            }
        };
        init = true;
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Generate option menu automaticly from xml
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Event executed if any menu item is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;

        switch (item.getItemId()) {
            case R.id.menu_help:
                i = new Intent(this, HelpActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

        }

        return true;
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     When activity finishes and goes back to this activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                break;
        }

    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Listen for configuration changes in orientation
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        /*if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            flashMessage("Landscape mode activated");
            ingameLayout = getGameplayLayout(R.layout.ingame);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            flashMessage("Portrait mode activated");
            ingameLayout = getGameplayLayout(R.layout.gameplay);
        }*/
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Check network connection and "reconnect" socket IO automaticly when connection
     *                  goes back.
     */
    private void checkNetwork()
    {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (!cm.getActiveNetworkInfo().isConnectedOrConnecting() && !init) {
                flashMessage("Network is down");
            }
            if (cm.getActiveNetworkInfo().isConnected()) {
                 getSocketHandler().connect(serverAddress);
            }
        } catch (Exception e) {
            Log.d("network", e.toString());
            if (!init) flashMessage("Network is down");
        }
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     When app is started from Androids life cycle
     *                  Reload last state and register broadcast receiver
     */
    @Override
    public void onResume()
    {
        registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        loadState();
        super.onResume();
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     When app is going into "sleep" mode in Androids life cycle
     *                  Save current state and unregister broadcast receiver
     */
    @Override
    public void onPause()
    {
        unregisterReceiver(receiver);
        saveState();
        super.onPause();
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     When app is destroyed from Androids life cycle
     *                  Save current active sessions, if any.
     */
    public void onDestroy()
    {
        getPreferences(MODE_PRIVATE).edit().putString(SESSION_DATA, sessionHandler.sessionsAsJsonString()).commit();
        super.onDestroy();
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Load last state from preferences and import sessions, as well as switch layout
     */
    private void loadState()
    {
        String data = getPreferences(MODE_PRIVATE).getString(SESSION_DATA, "");

        if (!data.isEmpty()) {
            sessionHandler = new SessionHandler(data);
            updateGameList(sessionHandler.getSessions());
        } else {
            sessionHandler = new SessionHandler();
        }

        if (sessionHandler.getSessions().size() > 0) {
            int layout = getPreferences(MODE_PRIVATE).getInt(LAYOUT_STATE, LAYOUTS.CHOOSEGAME.ordinal());
            switchToLayout(LAYOUTS.values()[layout]);
        } else {
            switchToLayout(LAYOUTS.CHOOSEGAME);
        }

        if (isLayoutState(LAYOUTS.INGAME)) {
            chat.setChatMessages(getPreferences(MODE_PRIVATE).getString(CHAT_DATA, ""));
        }
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Save current state
     */
    private void saveState()
    {
        SharedPreferences.Editor preferences = getPreferences(MODE_PRIVATE).edit();
        preferences.putString(SESSION_DATA, "").commit();
        preferences.putInt(LAYOUT_STATE, layoutState.ordinal()).commit();

        if (isLayoutState(LAYOUTS.INGAME)) {
            preferences.putString(CHAT_DATA, chat.getChatMessages()).commit();
        }
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Create layouts, init UI interaction and append to viewswitcher
     */
    private void createLayouts()
    {
        LayoutInflater li = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ingameLayout = li.inflate(R.layout.ingame, null);
        choosegameLayout = li.inflate(R.layout.choosegame, null);

        switcher = (ViewSwitcher)findViewById(R.id.ViewSwitcher);
        switcher.addView(choosegameLayout);
        switcher.addView(ingameLayout);

        initInGameUI();
        initChooseGameUI();
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Initiate game controls
     */
    public void initInGameUI()
    {
        InGameClickListener inGame = new InGameClickListener(this);
        Button playButton = (Button)findViewById(R.id.playMove);
        playButton.setOnClickListener(inGame);
        Button passButton = (Button)findViewById(R.id.passMove);
        passButton.setOnClickListener(inGame);
        Button swapButton = (Button)findViewById(R.id.swapMove);
        swapButton.setOnClickListener(inGame);
        Button backButton = (Button)findViewById(R.id.backButton);
        backButton.setOnClickListener(inGame);

        Button shuffleButton = (Button)findViewById(R.id.shuffle);
        shuffleButton.setOnClickListener(inGame);
        Button clearButton = (Button)findViewById(R.id.clearMoved);
        clearButton.setOnClickListener(inGame);

        HorizontalScrollView scroll = (HorizontalScrollView)findViewById(R.id.slider);
        //scroll.setOnDragListener(new DragViewListener(this, scroll));

        toggleKeyboard(false);
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Initiate choose game controls and update spinners
     */
    public void initChooseGameUI()
    {
        ChooseGameClickListener chooseGame = new ChooseGameClickListener(this);
        Button createGameButton = (Button)findViewById(R.id.createGame);
        createGameButton.setOnClickListener(chooseGame);
        Button joinGameButton = (Button)findViewById(R.id.joinGame);
        joinGameButton.setOnClickListener(chooseGame);

        gameList = new ArrayAdapter<GameSession>(
                Scrabble.this,
                android.R.layout.simple_spinner_item,
                new ArrayList<GameSession>());
        gameList.setNotifyOnChange(true);

        Spinner glist = (Spinner)findViewById(R.id.gameList);
        glist.setAdapter(gameList);

        languageList = new ArrayAdapter<String>(
                Scrabble.this,
                android.R.layout.simple_spinner_item,
                new ArrayList<String>());
        languageList.setNotifyOnChange(true);

        Spinner list = (Spinner)findViewById(R.id.languageList);
        list.setAdapter(languageList);
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Toggle keyboard shown or not
     */
    public void toggleKeyboard(boolean show)
    {
        EditText input = (EditText)findViewById(R.id.EditTextWriteMessage);
        //input.setInputType(0);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (show) {
            imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
        } else {
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        }
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Use viewswitcher to flip to correct layout
     */
    public synchronized void switchToLayout(LAYOUTS layout)
    {
        layoutState = layout;

        switch (layout) {
            case INGAME:
                switcher.setDisplayedChild(1);
                responseView = (TextView)findViewById(R.id.gameresponse);
                chat = new Chat(this);
                toggleKeyboard(false);
                break;
            case CHOOSEGAME:
                switcher.setDisplayedChild(0);
                if (socketHandler != null && init) {
                    socketHandler.sendMessage("gameinfo", getGameInfo());
                }
                responseView = (TextView)findViewById(R.id.selectresponse);
                break;
        }
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Show progress loader on longer events
     */
    public void showLoader(String status)
    {
        if (progressDialog == null || progressDialog.isShowing()) return;

        Log.d("loader", String.format("Trying do start progress dialog: %s", status));
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(status);
        progressDialog.show();
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Hide progress loader, if any
     */
    public void hideLoader(String status)
    {
        Log.d("loader", String.format("Trying do dismiss progress dialog: %s", status));
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        //flashMessage(status, false);
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Get current game info as json
     */
    public String getGameInfo()
    {
        GameSession session = getActiveSession();
        return session != null && session.getPlayerId() > 0 ? "{playerid: " + session.getPlayerId() + "}" : "";
    }

    @Override
    // This will make sure window is loaded before reading size from board.
    public void onWindowFocusChanged(boolean focused)
    {
        Log.d("scrabble", String.format("Window focus change, focused: %s", focused));

        if (init) {
            init = false;
        }
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Get socket handler
     */
    public synchronized SocketHandler getSocketHandler()
    {
        return socketHandler;
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Set response message and optionally flash message
     */
    public synchronized void flashMessage(String message, boolean flash)
    {
        setResponseMessage(message);
        if (flash) flashMessage(message);
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Flash message to toaster
     */
    public synchronized void flashMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Set response message on current responseview
     */
    public void setResponseMessage(String message)
    {
        responseView.setText(message);
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Set a new active session
     */
    public synchronized void setActiveSession(GameSession session)
    {
        this.sessionHandler.setActiveSession(session);
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Get current active session
     */
    public synchronized GameSession getActiveSession()
    {
        return sessionHandler.getActiveSession();
    }

    /**
     * @author          Joel Denke and Mathias Westman
     * @description     Send to server I am ready to start game
     */
    public synchronized void sendReady(GameSession session)
    {
        socketHandler.sendMessage("startgame", session.asJsonString());
    }

    public synchronized void updateChat(int playerid, String message)
    {
        if (chat instanceof Chat) {
            GameSession session = getActiveSession();
            chat.displayMessage(session.getPlayerLabel(playerid), message);

            if (playerid != getActiveSession().getPlayerId()) {
                flashMessage(String.format("Got message %s from playerid %d", message, playerid));
            }
        }
    }

    private int getDpAsPx(int dp)
    {
        final float scale = getResources().getDisplayMetrics().density;
        return (int)(dp * scale);
    }

    private Point getBoardSize(LinearLayout board, RelativeLayout chat)
    {
        View table = findViewById(R.id.gameTable);
        ViewGroup.LayoutParams tableParams = table.getLayoutParams();
        ViewGroup.LayoutParams boardParams = board.getLayoutParams();
        ViewGroup.LayoutParams chatParams = chat.getLayoutParams();
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Log.d("gameboard", String.format("Screen size [%d, %d]", size.x, size.y));

        tableParams.width = size.x - getDpAsPx(20);
        chatParams.width = (int)(0.5*size.x - 10);

        if (size.x < size.y) {
            boardParams.width  = size.x  - getDpAsPx(40);
            boardParams.height = size.y - getDpAsPx(10);
        } else {
            boardParams.width = (int)(0.7*tableParams.width);
            boardParams.height = size.y;
        }

        //TableRow row = (TableRow)findViewById(R.id.lastRow);
        Log.d("gameboard", String.format("Game board size [%d, %d]", boardParams.width, boardParams.height));

        return new Point(boardParams.width, boardParams.height);
    }

    private synchronized void updateGameBoard(int boardSize)
    {
        // Wait until the layouts are loaded into viewSwitcher, to get size for game board properly
        LinearLayout boardHolder = (LinearLayout)findViewById(R.id.board);
        RelativeLayout chatBoard = (RelativeLayout)findViewById(R.id.chat);
        HorizontalScrollView scroll1 = (HorizontalScrollView)findViewById(R.id.slider);
        ScrollView scroll2 = (ScrollView)findViewById(R.id.boardscroll);
        //board.initBoard(boardSize, getBoardSize());
        //board = new GameBoard(this, SIZE, size);

        this.board = new GameBoard(this, scroll1, scroll2, boardSize, getBoardSize(boardHolder, chatBoard));
        boardHolder.removeAllViews();
        boardHolder.addView(this.board);

        GameSession session = getActiveSession();
        this.board.updateBoard(session.getBoard());
        this.board.updateTileHolder(session.getUnPlayedTiles());
    }

    public synchronized void addPlayer(int id, int score)
    {
        GameSession session = getActiveSession();
        TextView textView = new TextView(this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        scoreBoard.addView(textView);
        scoreBoard.requestLayout();
        Player p = new Player(session.getPlayerLabel(id), score, textView);
        session.addPlayer(p);
    }

    private void createScoreBoard(JSONArray players)
    {
        scoreBoard = (LinearLayout)findViewById(R.id.scoreBoard);
        scoreBoard.removeAllViews();

        for (int i = 0; i < players.length(); i++) {
            try {
                 JSONObject player = players.getJSONObject(i);
                 addPlayer(player.getInt("playerid"), player.getInt("score"));
            } catch (JSONException e) {
                Log.d("json", e.toString());
            }
        }
    }

    public void startGame(JSONArray players, JSONArray tiles, JSONArray playedTiles, int turn, int size)
    {
        switchToLayout(LAYOUTS.INGAME);
        GameSession session = getActiveSession();

        session.initBoard(size);
        session.addUnplayedTiles(tiles);
        session.addTilesToBoard(playedTiles);
        session.setTurn(turn);

        flashMessage(String.format("Game started, it is %s!", session.turnAsString()), false);

        createScoreBoard(players);
        updateGameBoard(size);

        //session.printTiles();
        //session.printBoard();
    }

    public boolean isLayoutState(LAYOUTS state)
    {
        return state == layoutState;
    }

    private synchronized void updatePlayerScore(int score, int playerId)
    {
        GameSession session = getActiveSession();

        for (Player p: session.getPlayers()) {
            if (p.getName().equals(session.getPlayerLabel(playerId))) {
                p.setPoints(score);
            }
        }
    }

    public synchronized void updateScoreBoard(int score, int playerId)
    {
         updatePlayerScore(score, playerId);
    }

    public synchronized GameBoard getGameBoard()
    {
        return board;
    }

    public String languageAsLabel(String data)
    {
        if (data.equals("sv")) return "Swedish";
        if (data.equals("dev")) return "Developer";
        if (data.equals("en")) return "English";
        return "Unknown";
    }

    public synchronized SessionHandler getSessionHandler()
    {
        return sessionHandler;
    }

    public synchronized void updateLanguageList(ArrayList<String> languages)
    {
        Log.d("update", "Got new language list");
        languageList.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            languageList.addAll(languages);
        } else {
            for (String item: languages) {
                languageList.add(item);
            }
        }
    }

    public synchronized void updateGameList(ArrayList<GameSession> games)
    {
        // Need to be in correct layout state
        if (!isLayoutState(LAYOUTS.CHOOSEGAME) || init) return;

        Log.d("update", String.format("Got %d new games to append to game list", games.size()));

        if (games.size() > 0) {
            gameList.clear();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                gameList.addAll(games);
            }else{
                for (GameSession item: games) {
                   gameList.add(item);
                }
            }
        }
    }
}