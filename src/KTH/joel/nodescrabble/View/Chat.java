package KTH.joel.nodescrabble.View;

import KTH.joel.nodescrabble.R;
import KTH.joel.nodescrabble.Scrabble;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Chat implements View.OnClickListener
{
    private EditText editText;
    private TextView text;
    private Scrabble activity;

    public Chat (Scrabble activity)
    {
        this.activity = activity;
        editText = (EditText) activity.findViewById(R.id.EditTextWriteMessage);
        text = (TextView) activity.findViewById(R.id.TextViewDialog);

        Button button = (Button)activity.findViewById(R.id.ButtonSendMessage);
        button.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        activity.toggleKeyboard(false);
        activity.getSocketHandler().sendMessage("chatmessage",
                String.format("{playerid: %d, sessionid: %d, message: %s}",
                        activity.getActiveSession().getPlayerId(),
                        activity.getActiveSession().getId(),
                        extractMessage()));
        //displayMessage("Mathias", );
    }

    public void setChatMessages(String data)
    {
        text.setText(data);
    }

    public String getChatMessages()
    {
        return text.getText().toString();
    }

    public void displayMessage(String playerName, String message)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        text.append("("+ dateFormat.format(cal.getTime())+")\n");
        text.append(playerName+":"+ " "+ message);
        text.append("\n");
    }

    public String extractMessage()
    {
		 /*Retrieve what is in EditTextWriteMessage and display in TextViewDialog*/
        String message = editText.getText().toString();
        editText.setText("");
        return message;

    }
}
