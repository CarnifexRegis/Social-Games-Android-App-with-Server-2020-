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

public class GPSButton extends ButtonCustom{
    public static int IS_GPS = -1;
    public GPSButton(Context context) {
        super(context);
    }

    public GPSButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GPSButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //changes the layout of the button because every button has another layout that design looks good
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //draw rectangle based on gps settings
        Paint paintButton = new Paint();
        paintButton.setAntiAlias(true);
        Rect rect = new Rect(startX, startY, width, height);
        if(IS_GPS==0||IS_GPS==1||IS_GPS==2 || IS_GPS==3){
            if(IS_GPS==0) {
                paintButton.setColor(Color.BLUE);
            } else if(IS_GPS==2){
                paintButton.setColor(Color.YELLOW);
            } else if (IS_GPS == 1){
                paintButton.setColor(Color.RED);
            } else {
                paintButton.setColor(Color.GRAY);
            }
        } else{
            paintButton.setColor(Color.LTGRAY);
        }
        canvas.drawRect(rect, paintButton);
        //draw image over the rectangles
        Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.gps);
        Rect dstRectForRender = new Rect( startX+40, startY+10, width-40, height-10);

        canvas.drawBitmap ( b, null,dstRectForRender, null );
    }
}
