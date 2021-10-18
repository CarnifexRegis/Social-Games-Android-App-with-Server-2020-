package com.example.steppingmonsterduel2.ButtonViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.example.steppingmonsterduel2.R;

public class ButtonMatchmaking extends  ButtonCustom{
    public ButtonMatchmaking(Context context) {
        super(context);
    }

    public ButtonMatchmaking(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonMatchmaking(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //changes the layout of the button because every button has another layout that design looks good
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.matchmaking);
        Rect imgRect = new Rect( startX, startY, width, height);

        Path path = new Path();
        path.moveTo(width,heightRight);
        path.lineTo(widthBottom,height);
        path.lineTo(width,height);
        path.lineTo(width,heightRight);
        path.close();

        Path path2 = new Path();
        path2.moveTo(startX,startY);
        path2.lineTo(width-widthBottom,startY);
        path2.lineTo(0,height-heightRight);
        path2.lineTo(startX,startY);

        canvas.drawBitmap ( b, null, imgRect, null );

        if(isClicked){
            canvas.drawRect(imgRect,paintOnClick);
            isClicked = false;
            invalidate();
        }
        canvas.drawPath(path,paint);
        canvas.drawPath(path2,paint);
    }
}
