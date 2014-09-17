package KTH.joel.nodescrabble.View;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Grid
{
	private int gridX, gridY;
	private int color;
    private int size;
    private int x, y;

    public Grid(int gridSize, int x, int y, int color)
    {
        this.x = gridSize * x;
        this.y = gridSize * y;
        this.gridX = x;
        this.gridY = y;
        this.color = color;
        this.size  = gridSize;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public Rect asRect()
    {
        return new Rect(x, y, x + size, y + size);
    }

    public void draw(Canvas canvas, Paint paint)
    {
        Rect r = asRect();
        paint.setColor(Color.BLACK);
        canvas.drawRect(r, paint);
        r.inset(1, 1);
        paint.setColor(color);
        canvas.drawRect(r, paint);
    }
	
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

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
