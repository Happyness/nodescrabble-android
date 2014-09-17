package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.GameSession;
import KTH.joel.nodescrabble.R;
import KTH.joel.nodescrabble.Scrabble;
import KTH.joel.nodescrabble.SocketHandler;
import KTH.joel.nodescrabble.View.GameItem;
import KTH.joel.nodescrabble.View.LanguageItem;
import android.view.View;
import android.widget.Spinner;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 2014-01-02
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class ChooseGameClickListener implements View.OnClickListener
{
    private Scrabble activity;

    public ChooseGameClickListener(Scrabble activity)
    {
        this.activity = activity;
    }
    @Override
    public void onClick(View v)
    {
        SocketHandler socket = activity.getSocketHandler();
        if (socket == null) {
            activity.flashMessage("Socket is not available yet!", false);
            return;
        }

        switch (v.getId()) {
            case R.id.createGame :
                Spinner languageList = (Spinner)activity.findViewById(R.id.languageList);
                String language = (String)languageList.getSelectedItem();

                if (language != null) {
                    socket.sendMessage("initgame", String.format(
                            "{language: '%s'}",
                            language
                    ));
                    activity.flashMessage("Waiting for server to response on create game", false);
                } else {
                    activity.flashMessage("You need to select game session to join a game", false);
                }
                break;
            case R.id.joinGame:
                Spinner gameList = (Spinner)activity.findViewById(R.id.gameList);
                GameSession session = (GameSession)gameList.getSelectedItem();

                if (session != null) {
                    activity.setActiveSession(session);
                    socket.sendMessage("joingame", session.asJsonString());
                    activity.flashMessage("Waiting for server to response on join game", false);
                } else {
                    activity.flashMessage("You need to select language to create a new game", false);
                }
                break;
        }
    }
}
