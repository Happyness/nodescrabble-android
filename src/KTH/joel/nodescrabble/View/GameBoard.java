package KTH.joel.nodescrabble.View;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.*;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


public class GameBoard extends View
{
    private enum GridStyle {
        TL, DL, TW, DW, CENTER, DEFAULT
    };

    private int gridSize;
    private Paint paint = new Paint();
    private Paint textpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Grid grid[][];
    private int returnPosX=0, returnPosY=0;
    private Tile tileMoving;
    private boolean swapMode = false;
    private ArrayList<Tile> tiles = new ArrayList<Tile>();
    private int boardSize, tileHolderSize, tileSize, stepSize;
    private Point totalSize;
    private int size = 15, noTiles = 7;
    private Rect tileHolder, boardRect;
    private HorizontalScrollView scroll1;
    private ScrollView scroll2;
    private boolean vertical = false;

    public GameBoard(Context context, HorizontalScrollView scroll1, ScrollView scroll2, int matrixSize, Point size)
    {
        super(context);

        this.scroll1 = scroll1;
        this.scroll2 = scroll2;
        this.totalSize = size;
        this.size = matrixSize;

        setup(size);
        createGrid();

        invalidate();
    }

    public void setup(Point size)
    {
       /* Get board size
        *  - Set size from the maximum size, depending on orientation
        */
        if (size.y > size.x) {
            this.boardSize = size.x;
            this.tileHolderSize = size.y - (int)(0.8*boardSize);
            vertical = true;
        } else {
            this.boardSize = size.y;
            this.tileHolderSize = size.x - (int)(0.8*boardSize);
            vertical = false;
        }

        Log.d("gameboard", String.format("Size: [%d]", boardSize));

        this.gridSize = boardSize / this.size;
        this.stepSize = boardSize / noTiles;
        this.tileSize = stepSize - 12;

        Log.d("gameboard", String.format("Grid size: %d", gridSize));
        Log.d("gameboard", String.format("Tile size: %d", tileSize));
        Log.d("gameboard", String.format("Step size: %d", stepSize));

        if (vertical) {
            tileHolder = new Rect(0, boardSize, boardSize, boardSize + tileHolderSize);
            boardRect  = new Rect(0, 0, boardSize, boardSize + tileHolderSize);
        } else {
            tileHolder = new Rect(boardSize, 0, boardSize + tileHolderSize, boardSize);
            boardRect  = new Rect(0, 0, boardSize + tileHolderSize, boardSize);
        }
    }

    private void toggleScrolls(boolean enableInteract)
    {
        if (enableInteract) {
            scroll1.requestDisallowInterceptTouchEvent(true);
            scroll2.requestDisallowInterceptTouchEvent(true);
        } else {
            scroll1.requestDisallowInterceptTouchEvent(false);
            scroll2.requestDisallowInterceptTouchEvent(false);
        }
    }

    private void updateTileSize()
    {
        for (Tile t: tiles) {
            if (!t.isState(Tile.STATE.UNTOUCHED)) {
                t.setSize(gridSize);
                t.setPos((t.getGridX() + 1) * gridSize, (t.getGridY() + 1) * gridSize);
            } else {
                Rect r = getHolderRect(t.getHolderPos());
                t.setPos(r.left, r.top);
                t.setSize(tileSize);
            }
        }

        shuffleTiles();
    }

    private void updateGridSize()
    {
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                Grid g = grid[y][x];
                g.setSize(gridSize);
                g.setX(gridSize * x);
                g.setY(gridSize * y);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d("gameboard", "Game.onSizeChanged: width " + w + ", height " + h);

        /*
        Point p;
        if (w > h) {
            p = new Point(h, w);
        } else {
            p = new Point(w, h);
        }*/
        setup(new Point(w, h));
        updateGridSize();
        updateTileSize();
        //discardTiles();
        invalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int getRGBFromHex(String hex)
    {
        int r = Integer.valueOf(hex.substring(1, 3), 16);
        int g = Integer.valueOf(hex.substring(3, 5), 16);
        int b = Integer.valueOf(hex.substring(5, 7), 16);
        return Color.rgb(r, g, b);
    }

    public int getColor(GridStyle style)
    {
        switch (style) {
            case TW: return getRGBFromHex("#9F4718");
            case TL: return getRGBFromHex("#1B8FA1");
            case DW: return getRGBFromHex("#CF8025");
            case DL: return getRGBFromHex("#76A86B");
            case CENTER: return getRGBFromHex("#664466");
            default: return getRGBFromHex("#595959");
        }
    }

    public Rect getTileHolderRect()
    {
        return tileHolder;
    }

    public void createGrid()
    {
        grid = new Grid[size][size];

        for(int y = 1; y <= size; y++) {
            for(int x = 1; x <= size; x++) {
                int color;

                if (((x == 1 || x == 15) && (y == 1 || y == 15))) {
                    color = getColor(GridStyle.TW);
                }
                else if (((x == 2 || x == 14) && (y == 6 || y == 10))) {
                    color = getColor(GridStyle.TL);
                }
                else if (((x == 4 || x == 12) && (y == 4 || y == 12))) {
                    color = getColor(GridStyle.TL);
                }
                else if (((x == 6 || x == 10) && (y == 2 || y == 6 || y == 10 || y == 14))) {
                    color = getColor(GridStyle.TL);
                }
                else if (((x == 1 || x == 15) && (y == 5 || y == 11))) {
                    color = getColor(GridStyle.TW);
                }
                else if (((x == 5 || x == 11) && (y == 1 || y == 15))) {
                    color = getColor(GridStyle.TW);
                }
                else if ((x == 1 || x == 15) && y == 8) {
                    color = getColor(GridStyle.TW);
                }
                else if ((x == 2 || x == 14) && (y == 2 || y == 14)) {
                    color = getColor(GridStyle.DL);
                }
                else if ((x == 3 || x == 5 || x == 11 || x == 13) && (y == 7 || y == 9)) {
                    color = getColor(GridStyle.DL);
                }
                else if ((x == 7 || x == 9) && (y == 3 || y == 5 || y == 11 || y == 13)) {
                    color = getColor(GridStyle.DL);
                }
                else if ((x == 8) && (y == 1 || y == 15)) {
                    color = getColor(GridStyle.DL);
                }
                else if ((x == 3 || x == 13) && (y == 3 || y == 13)) {
                    color = getColor(GridStyle.DW);
                }
                else if ((x == 4 || x == 12) && (y == 8)) {
                    color = getColor(GridStyle.DW);
                }
                else if ((x == 5 || x == 11) && (y == 5 || y == 11)) {
                    color = getColor(GridStyle.DW);
                }
                else if ((x == 8) && (y == 4 || y == 12)) {
                    color = getColor(GridStyle.DW);
                }
                else if ((x == 8) && (y == 4 || y == 12)) {
                    color = getColor(GridStyle.DW);
                }
                else if ((x == 8) && (y == 8)) {
                    color = getColor(GridStyle.CENTER);
                }
                else {
                    color = getColor(GridStyle.DEFAULT);
                }

                grid[y-1][x-1] = new Grid(gridSize, x-1, y-1, color);
            }
        }
    }

    public boolean isSwapMode()
    {
        return this.swapMode;
    }

    public void setSwapMode(boolean mode)
    {
        this.swapMode = mode;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        drawGrid(canvas);
        drawTiles(canvas);
    }

    private void drawGrid(Canvas canvas)
    {
        /*draw grid background*/
        for(int y=0;y < size;y++)
            for(int x=0;x < size;x++)
            {
                Grid g = grid[y][x];
                g.draw(canvas, paint);
            }
    }

    private int getScaledTextSize(String s, Rect tile, int scale)
{
        int size = 0;
        Paint paint = new Paint();
        Rect bounds = new Rect();
        int width = tile.width() / scale, height = tile.height() / scale;

        do {
            paint.setTextSize(++size);
            paint.getTextBounds(s, 0, 1, bounds);
        } while (bounds.width() < width && bounds.height() < height);

        return size;
    }

    private Point getLetterPosition(Rect t)
    {
        int x = t.centerX();
        int y = t.centerY() + t.width() / 4;
        return new Point(x, y);
    }

    private Point getScorePosition(Rect t)
    {
        int x = t.right - t.width() / 10;
        int y = t.bottom - t.height() / 10;
        return new Point(x, y);
    }

    private int drawTile(Canvas canvas, Tile t)
    {
        if (t.isEmpty()) return 0;

        int textSize = 1000, tmp;
        String letter = Character.toString(t.getLetter());
        String score = Integer.toString(t.getValue());
        Rect tile = tileAsRect(t);

        // Draw border around tile
        paint.setColor(Color.BLACK);
        canvas.drawRect(tile, paint);

        // Draw fill color in tile
        if (t.isState(Tile.STATE.SWAP)) {
            paint.setColor(getRGBFromHex("#BB6008"));
        } else if (t.isState(Tile.STATE.PLAYED)) {
            paint.setColor(getRGBFromHex("#FFFF19"));
        } else {
            paint.setColor(Color.WHITE);
        }
        tile.inset(2, 2);
        canvas.drawRect(tile, paint);

        // Dynamic text size depending on tile size
        tmp = getScaledTextSize(letter, tile, 2);
        if (tmp < 1000) textSize = tmp;

        // Draw letter
        textpaint.setColor(Color.BLACK);
        textpaint.setTextAlign(Paint.Align.CENTER);
        textpaint.setTextSize(textSize);
        Point p = getLetterPosition(tile);
        canvas.drawText(letter, p.x, p.y, textpaint);

        // Draw score
        textpaint.setTextAlign(Paint.Align.RIGHT);
        textpaint.setTextSize(getScaledTextSize(score, tile, 5));
        Point p2 = getScorePosition(tile);
        canvas.drawText(score, p2.x, p2.y, textpaint);

        return textSize;
    }

    private void drawTiles(Canvas canvas)
    {
        // Make text look crystal clear
        int textSize = 1000, tmp;

        for(Tile t: tiles)
        {
            textSize = drawTile(canvas, t);
        }
    }

    /**
     * @description: Calculate overlapping area between two rects
     * Code is borrowed/inspired by:
     * http://stackoverflow.com/questions/15299408/getting-the-area-of-two-intersecting-rectangles
     */
   private int intersectionArea(Rect r1, Rect r2)
   {
        int newLeft = Math.max(r1.left, r2.left);
        int newTop = Math.max(r1.top, r2.top);
        int width = Math.min(r1.right, r2.right) - newLeft;
        int height = Math.min(r1.bottom, r2.bottom) - newTop;

        if (width <= 0d || height <= 0d) return 0;

        return getRectArea(new Rect(newLeft, newTop, newLeft + width, newTop + height));
    }

    private int getRectArea(Rect r)
    {
        return r.width() * r.height();
    }

    private Grid getMostIntersecting(ArrayList<Grid> grids, Rect r)
    {
        int area = 0, tmpArea;
        Grid most = null;

        for (Grid g: grids) {
            tmpArea = intersectionArea(g.asRect(), r);
            if (tmpArea > area) {
                area = tmpArea;
                most = g;
            }

            Log.d("gameboard", String.format("Intersecting area [%d, %d]: %d", g.getGridX(), g.getGridY(), tmpArea));
        }

        Log.d("gameboard", String.format("Most intersecting area [%d, %d]", most.getGridX(), most.getGridY()));

        return most;
    }

    private boolean pointIntersect(Point p, Rect r)
    {
         return r.contains(p.x, p.y);
    }

    private boolean isTileInList(int index)
    {
        for (int i = 0; i < tiles.size(); i++) {
            if (index == i) return true;
        }

        return false;
    }

    private Rect getHolderRect(int steps)
    {
        int px, py;

        if (vertical) {
            px = steps * this.stepSize;
            py = this.boardSize + 2;
        } else {
            py = steps * this.stepSize;
            px = this.boardSize + 2;
        }

        return new Rect(px, py, px + tileSize, py + tileSize);
    }


    private boolean intersectTileOnGrid(int x, int y)
    {
        for (Tile t: tiles) {
            if (y == t.getGridY() && x == t.getGridX()) {
                Log.d("gameboard", String.format("[%d, %d] intersecting grid [%d, %d]", t.getGridX(), t.getGridY(), x, y));
                return true;
            } else {
                Log.d("gameboard", String.format("[%d, %d] not intersecting grid [%d, %d]", t.getGridX(), t.getGridY(), x, y));
            }
        }

        return false;
    }

    private boolean isIntersectHolderTile(Rect r)
    {
        ArrayList<Tile> tiles = getTiles(Tile.STATE.UNTOUCHED);
        //Log.d("gameboard", String.format("No tiles in holder %d", tiles.size()));

        for (Tile t: tiles) {
            Rect tile = tileAsRect(t);
            //Log.d("gameboard", String.format("Holder tile [%d, %d]", tile.left, tile.top));
            if (tile.contains(r.left, r.top, r.right, r.bottom)) return true;
        }

        return false;
    }

    private Point getFirstAvailablePosition()
    {
        Rect r1, r2;
        ArrayList<Tile> tiles = getTiles(Tile.STATE.UNTOUCHED);

        for (int pos = 0; pos < noTiles; pos++)
        {
            boolean skip = false;
            r1 = getHolderRect(pos);

            for (Tile t: tiles)
            {
                if (skip) break;

                r2 = tileAsRect(t);

                //Log.d("gameboard", String.format("Holder tile [%d, %d, %d, %d] compare to tile [%d, %d, %d, %d]",
                //        r1.left, r1.top, r1.right, r1.bottom, r2.left, r2.top, r2.right, r2.bottom));

                if (r2.intersect(r1) && r2.contains(r1) && (r1.right == r2.right && r1.bottom == r2.bottom)) {
                    skip = true; // If any holder tile intersects, skip current tile holder
                }
            }

            if (!skip) return new Point(r1.left, r1.top);
        }

        //Log.d("gameboard", String.format("No available pos"));

        return null;
    }

    private Point getHolderPoint(Tile tile)
    {
        int i = 0;

        for(Tile t: tiles)
        {
            Rect r1 = getHolderRect(i);
            Rect r2 = tileAsRect(t);

            if ((!isTileInList(i) && r1.intersect(r2))) {
                 return null;
            }

            if (r1.contains(tile.getX(), tile.getY())) {
                return new Point(r1.left, r1.top);
            }

            i++;
        }

        return null;
    }

    public boolean tileIntersectBoard(Tile t)
    {
        Rect tile = tileAsRect(t);

        /*Log.d("gameboard",
            String.format(
            "Tile [%d, %d, %d, %d] intersects board [%d, %d, %d, %d]",
            tile.left, tile.top, tile.right, tile.bottom, boardRect.left, boardRect.top, boardRect.right, boardRect.bottom
        ));*/
        return boardRect.contains(tile.left, tile.top, tile.right, tile.bottom);
    }

    public Point tileIntersectHolder(Tile t)
    {
        Rect tile = tileAsRect(t);
        Rect holder = getTileHolderRect();

        if (tile.intersect(holder)) {
            return getHolderPoint(t);
        }

        return null;
    }

    public Grid tileIntersectGrid(Tile t)
    {
        ArrayList<Grid> intersecting = new ArrayList<Grid>();
        Rect tile = tileAsRect(t);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Grid g = grid[y][x];

                if (tile.intersect(g.asRect())) {
                    intersecting.add(g);
                }
            }
        }

        if (intersecting.size() > 0) {
            return getMostIntersecting(intersecting, tile);
        }

        return null;
    }

    private boolean isOverGrid(Tile t)
    {
        return t.getX() <= boardSize && t.getY() <= boardSize && t.getX() >= 0 && t.getY() >= 0;
    }

    private Rect tileAsRect(Tile t)
    {
        int size = t.getSize();
        return new Rect(t.getX(), t.getY(), t.getX() + size, t.getY() + size);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        Point p = new Point((int)e.getX(), (int)e.getY());
        //Log.d("gameboard", String.format("Moving pointer: [%d, %d]", p.x, p.y));

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                //Log.d("gameboard", "Action down");
                if (isSwapMode()) {
                    swapTile(p);
                } else {
                    initDrag(p);
                }
                break;
            //}
            case MotionEvent.ACTION_MOVE:
                //Log.d("gameboard", "Action move");
                if (!isSwapMode()) {
                    dragTile(p);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            //case MotionEvent.ACTION_CANCEL:
                //Log.d("gameboard", "Action up");
                if (!isSwapMode()) {
                    dropTile();
                }
                break;
        }
        invalidate();
        return true;
    }

    public void discardTiles()
    {
        for (Tile t: tiles) {
            Point p = getFirstAvailablePosition();

            if (t.isState(Tile.STATE.MOVED) && p != null) {
                t.setStartposX(p.x);
                t.setStartposX(p.y);
                t.setState(Tile.STATE.UNTOUCHED);
                t.setPos(p.x, p.y);
                t.setSize(tileSize);
            }
        }
        invalidate();
    }

    public void shuffleTiles()
    {
        ArrayList<Tile> list = getTiles(Tile.STATE.UNTOUCHED);
        Collections.shuffle(list);
        int i = 0;

        for(Tile t: list)
        {
            //Log.d("gameboard", String.format("Shuffle and put on position %d", i));
            Rect r = getHolderRect(i);
            t.setX(r.left);
            t.setY(r.top);
            i++;
        }
        invalidate();
    }

    private void swapTile(Point p)
    {
        for (Tile t: tiles) {
            Rect tile = tileAsRect(t);

            if (pointIntersect(p, tile))
            {
                if (t.isState(Tile.STATE.UNTOUCHED)) {
                    t.setState(Tile.STATE.SWAP);
                } else if (t.isState(Tile.STATE.SWAP)) {
                    t.setState(Tile.STATE.UNTOUCHED);
                }
                break;
            }
        }
    }

    private void initDrag(Point p)
    {
        if (tileMoving == null) {
            for (Tile t: tiles) {
                Rect tile = tileAsRect(t);

                if (pointIntersect(p, tile) && t.isMoveable())
                {
                    returnPosX = t.getX();
                    returnPosY = t.getY();
                    tileMoving = t;

                    toggleScrolls(true);
                    return;
                }
            }
        }
    }

    private void updateTileSize(Tile t)
    {
        if (isOverGrid(t)) {
            t.setSize(gridSize);
        } else {
            t.setSize(tileSize);
        }
    }

    private void centerTileAroundPoint(Tile t, Point p)
    {
        Rect r = tileAsRect(t);
        int diff = r.width() / 2;

        //if (p.x > r.centerX() && p.y > r.centerY()) {
        //    t.setPos(p.x - diff, p.y + diff);
        //} else if (p.x > r.centerX() && p.y < r.centerY()) {
        //    t.setPos(p.x - diff, p.y - diff);
        //} else if (p.x < r.centerX() && p.y > r.centerY()) {
        //    t.setPos(p.x + diff, p.y + diff);
        //} else if (p.x < r.centerX() && p.y < r.centerY()) {
        //    t.setPos(p.x + diff, p.y - diff);
        //} else {
            t.setPos(p.x, p.y);
        //}
    }

    private void dragTile(Point p)
    {
        if (tileMoving != null)
        {
            Point before = new Point(tileMoving.getX(), tileMoving.getY());
            centerTileAroundPoint(tileMoving, p);

            if (!tileIntersectBoard(tileMoving)) {
                tileMoving.setX(before.x);
                tileMoving.setY(before.y);
            }

            //updateTileSize(tileMoving);
            tileMoving.setSize(tileSize);
        }
    }

    private boolean isSameRects(Rect r1, Rect r2)
    {
        return (r1.left == r2.left && r1.top == r2.top && r1.right == r2.right && r1.bottom == r2.bottom);
    }

    private boolean intersectTile(Rect r)
    {
        for(Tile t: tiles)
        {
            Rect tile = tileAsRect(t);
            // Make sure we do not compare same rectangle ;)
            if (!isSameRects(tile, r) && r.intersect(tile)) {
                /*Log.d("gameboard",
                    String.format(
                    "Tile [%d, %d, %d, %d] intersects tile [%d, %d, %d, %d]",
                    tile.left, tile.top, tile.right, tile.bottom, r.left, r.top, r.right, r.bottom
                ));*/
                return true;
            }
        }

        return false;
    }

    private void discardTileMove(Tile t)
    {
        t.setPos(returnPosX, returnPosY);
        updateTileSize(t);

        for (int i = 0; i < noTiles; i++) {
            Rect r = getHolderRect(i);
            if (r.left == returnPosX && r.top == returnPosY) {
                t.setGridX(-1);
                t.setGridY(-1);
            }
        }
    }

    private void dropTile()
    {
        if(tileMoving != null)
        {
            Tile t = tileMoving;
            Point p = tileIntersectHolder(t);
            Grid g = tileIntersectGrid(t);

            if (g != null) {
                if (intersectTileOnGrid(g.getGridX(), g.getGridY())) {
                    discardTileMove(t);
                } else {
                    t.setGridX(g.getGridX());
                    t.setGridY(g.getGridY());
                    t.setX(g.getX());
                    t.setY(g.getY());
                    t.setStartposX(g.getX());
                    t.setStartposY(g.getY());
                    t.setState(Tile.STATE.MOVED);
                    t.setSize(gridSize);
                }
            } else if (p != null) {
                Point first = getFirstAvailablePosition();

                if (first != null) {
                    t.setSize(tileSize);
                    t.setX(first.x);
                    t.setY(first.y);
                    t.setStartposX(first.x);
                    t.setStartposY(first.y);
                    t.setGridX(-1);
                    t.setGridY(-1);
                    t.setState(Tile.STATE.UNTOUCHED);
                } else {
                   discardTileMove(t);
                }
            } else {
                discardTileMove(t);
            }

            toggleScrolls(false);
            tileMoving = null;
        }
    }

    public void updateMoved()
    {
        for (Tile t: tiles) {
            if(t.isState(Tile.STATE.MOVED))
            {
                t.setState(Tile.STATE.PLAYED);
            }
        }
    }

    public Tile getTileOnPos(int y, int x)
    {
        for (Tile t: tiles) {
            if (t.getGridX() == x && t.getGridY() == y) {
                return t;
            }
        }

        return null;
    }

    public Grid getGridOnPos(int y, int x)
    {
        return grid[y][x];
    }

    public void moveToHolder(Tile t)
    {
        t.setSize(tileSize);
        t.setState(Tile.STATE.UNTOUCHED);
        t.setX(t.getStartposX());
        t.setY(t.getStartposY());
    }

    public synchronized void addPlayedTile(Tile t, Grid g)
    {
        Tile tile = new Tile(t.getLetter(), t.getValue(), t.getGridX(), t.getGridY());
        tile.setX(g.getX());
        tile.setY(g.getY());
        tile.setState(Tile.STATE.PLAYED);
        tile.setSize(gridSize);
        tiles.add(tile);
    }

    public void updateBoard(Tile[][] board)
    {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Tile tile = board[y][x];

                if (tile != null) {
                    Tile gridTile = getTileOnPos(y, x);
                    Grid g = getGridOnPos(y, x);
                    // Move back to tile holder if new tile get to same position on grid
                    if (gridTile != null && !gridTile.isState(Tile.STATE.PLAYED)) {
                       moveToHolder(gridTile);
                    }

                    addPlayedTile(tile, g);
                }
            }
        }
        invalidate();
    }

    private int countTiles(Tile.STATE state)
    {
        int count = 0;
        for (Tile t: tiles) {
            if (t.isState(state)) count++;
        }
        return count;
    }

    public void updateTileHolder(ArrayList<Tile> tiles)
    {
        for (Tile t: tiles) {
            Point p = getFirstAvailablePosition();

            if (p != null) {
                t.setSize(tileSize);
                t.setX(p.x);
                t.setY(p.y);
                t.setStartposX(p.x);
                t.setStartposY(p.y);
                t.setState(Tile.STATE.UNTOUCHED);
                this.tiles.add(t);
            }
        }

        invalidate();
    }

    public void removeTiles(Tile.STATE state)
    {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext())
        {
            Tile t = it.next();
            if (t.isState(state)) it.remove();
        }
    }

    public ArrayList<Tile> getTiles(Tile.STATE state)
    {
        ArrayList<Tile> tileList = new ArrayList<Tile>();

        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext())
        {
            Tile t = it.next();
            if (t.isState(state)) tileList.add(t);
        }

        return tileList;
    }
}