package KTH.joel.nodescrabble.View;

import android.widget.TextView;

public class Player
{
	private String name;
	private int points;
    private TextView board;
	
	public Player(String name, int points, TextView textView)
	{
		this.name = name;
		this.points = points;
        this.board = textView;

        this.board.setText(getValue());
	}
	
	public String getName()
	{
		return name;
	}

    public void setPoints(int sum)
    {
        points = sum;
        board.setText(getValue());
    }

    public String getValue()
    {
        return String.format("%s: %d", name, points);
    }

    @Override
	public String toString()
	{
		return getValue();
	}

}
