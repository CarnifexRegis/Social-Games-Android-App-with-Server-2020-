package com.example.steppingmonsterduel2.ButtonViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.example.steppingmonsterduel2.R;

public class ButtonEditCard extends ButtonCustom{

    public ButtonEditCard(Context context) {
        super(context);
    }

    public ButtonEditCard(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonEditCard(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //changes the layout of the button because every button has another layout that design looks good
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);



        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.editpicture);
        Rect imgRect = new Rect( startX, startY, width, height);

        Path path = new Path();
        path.moveTo(width,heightRight);
        path.lineTo(widthBottom,height);
        path.lineTo(width,height);
        path.lineTo(width,heightRight);
        path.close();

        canvas.drawBitmap ( b, null, imgRect, null );
        if(isClicked){
            canvas.drawRect(imgRect,paintOnClick);
            isClicked = false;
            invalidate();
        }
        canvas.drawPath(path,paint);
    }
}
