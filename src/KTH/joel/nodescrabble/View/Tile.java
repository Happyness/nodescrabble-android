package KTH.joel.nodescrabble.View;

public class Tile {
	
	private int x = 0, y = 0;
	private int value;
	private char letter;
	private STATE state = STATE.UNTOUCHED;
	private int startposX = 0, startposY = 0;
    private int gridX = -1, gridY = -1;
    private int size;
    private int holderPos = 0;

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getHolderPos() {
        return holderPos;
    }

    public void setHolderPos(int holderPos) {
        this.holderPos = holderPos;
    }

    public enum STATE {
         UNTOUCHED, MOVED, SWAP, PLAYED
    }

    public Tile(char letter)
    {
        this.letter = letter;
    }

    public Tile(char value, int score)
    {
        this.letter = value;
        this.value = score;
    }

    public Tile(char value, int score, int x, int y)
    {
        this.letter = value;
        this.value = score;
        this.gridX = x - 1;
        this.gridY = y - 1;
    }

    public boolean isEmpty()
    {
        return Character.isWhitespace(letter);
    }

    public boolean isMoveable()
    {
		switch (state) {
            case UNTOUCHED:
            case MOVED:
                return true;
            default:
                return false;
        }
	}

	public void setState(STATE state) {
		this.state = state;
	}

    public boolean isState(STATE state)
    {
        return this.state == state;
    }

    public void setPos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

	public int getStartposX() {
		return startposX;
	}

	public void setStartposX(int startposX) {
		this.startposX = startposX;
	}


	public int getStartposY() {
		return startposY;
	}

	public void setStartposY(int startposY) {
		this.startposY = startposY;
	}
	
	public char getLetter() {
		return letter;
    }

	public void setLetter(char letter) {
		this.letter = letter;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue()
	{
		return value;
	}

	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	
	public void setX(int x)
	{
		this.x = x;
	}
	
	public void setY(int y)
	{
		this.y = y;
	}
}
