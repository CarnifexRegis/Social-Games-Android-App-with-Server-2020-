package com.example.steppingmonsterduel2.ButtonViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.example.steppingmonsterduel2.R;

public class ButtonEditDeck extends ButtonCustom {
    public ButtonEditDeck(Context context) {
        super(context);
    }

    public ButtonEditDeck(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonEditDeck(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //changes the layout of the button because every button has another layout that design looks good
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Path path = new Path();
        path.moveTo(widthBottom,startY);
        path.lineTo(width,0);
        path.lineTo(width,height-heightRight);
        path.lineTo(widthBottom,startY);
        path.close();




        //canvas.drawText("Edit Deck",width/2,height/2,paint2);

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.editdeck);
        Rect imgRect = new Rect( startX, startY, width, height);

        canvas.drawBitmap ( b, null, imgRect, null );
        if(isClicked){
            canvas.drawRect(imgRect,paintOnClick);
            isClicked = false;
            invalidate();
        }
        canvas.drawPath(path,paint);
    }
}
