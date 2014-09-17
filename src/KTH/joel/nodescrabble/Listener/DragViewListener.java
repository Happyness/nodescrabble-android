package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.Scrabble;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 2014-01-12
 * Time: 09:06
 * To change this template use File | Settings | File Templates.
 */
public class DragViewListener implements View.OnDragListener
{
    private Scrabble activity;
    private HorizontalScrollView scroller;

    public DragViewListener(Scrabble main, HorizontalScrollView scroller)
    {
        this.activity = main;
        this.scroller = scroller;
    }

    @Override
    public boolean onDrag(View view, DragEvent event) {

        //MainWidget dropZoneView = (MainWidget) view;

        int action = event.getAction();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                activity.toggleKeyboard(true);
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                int left = view.getLeft();
                int right = view.getRight();

                int scrollX = scroller.getScrollX();
                int scrollViewWidth = scroller.getMeasuredWidth();

                Log.d("drag", "location: Scroll X: " + scrollX + " Scroll X+Height: " + (scrollX + scrollViewWidth));
                Log.d("drag"," left: "+ left +" right: "+right);

                if (right > (scrollX + scrollViewWidth - 100))
                    scroller.smoothScrollBy(0, 30);

                if (left < (scrollX + 100))
                    scroller.smoothScrollBy(0, -30);

                break;
            default:
                break;
        }
        return true;
    }
}
