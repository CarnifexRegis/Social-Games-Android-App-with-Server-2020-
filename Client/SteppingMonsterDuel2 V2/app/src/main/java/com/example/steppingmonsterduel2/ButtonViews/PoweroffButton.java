package com.example.steppingmonsterduel2.ButtonViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.example.steppingmonsterduel2.R;

public class PoweroffButton extends ButtonCustom{
    public PoweroffButton(Context context) {
        super(context);
    }

    public PoweroffButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PoweroffButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //changes the layout of the button because every button has another layout that design looks good
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        Paint paintButton = new Paint();
        paintButton.setAntiAlias(true);
        paintButton.setColor(Color.LTGRAY);
        Rect rect = new Rect(startX,startY,width,height);
        canvas.drawRect(rect,paintButton);

        Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.poweroff);
        Rect dstRectForRender = new Rect( startX+55, startY+20, width-55, height-20);

        canvas.drawBitmap ( b, null, dstRectForRender, null );

        if(isClicked){
            canvas.drawRect(rect,paintOnClick);
            isClicked = false;
            invalidate();
        }
    }
}
