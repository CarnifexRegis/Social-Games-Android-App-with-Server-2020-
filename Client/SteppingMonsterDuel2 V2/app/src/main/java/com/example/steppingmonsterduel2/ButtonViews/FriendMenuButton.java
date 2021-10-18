package com.example.steppingmonsterduel2.ButtonViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.example.steppingmonsterduel2.R;

public class FriendMenuButton extends ButtonCustom {
    public FriendMenuButton(Context context) {
        super(context);
    }

    public FriendMenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public FriendMenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //changes the layout of the button because every button has another layout that design looks good
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.friendlist);
        Rect imgRect = new Rect( startX, startY, width, height);


        canvas.drawBitmap ( b, null, imgRect, null );
        if(isClicked){
            canvas.drawRect(imgRect,paintOnClick);
            isClicked = false;
            invalidate();
        }
    }
}
