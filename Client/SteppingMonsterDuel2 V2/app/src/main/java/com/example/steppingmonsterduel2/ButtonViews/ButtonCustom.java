package com.example.steppingmonsterduel2.ButtonViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.steppingmonsterduel2.R;

abstract class ButtonCustom extends View {
    //initialize button attributes as the color of the button the start of the button
    protected int startX = 10;
    protected int startY = 10;
    protected int width;
    protected int height;
    protected Paint paint = new Paint();
    protected Paint paint2 = new Paint();
    protected Paint paintOnClick = new Paint();
    protected boolean isClicked;

    protected int widthBottom;
    protected int heightRight;
    public ButtonCustom(Context context) {
        super(context);
        initialize();
    }

    public ButtonCustom(Context context,  AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ButtonCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    //intialize paint attributes as color text
    private void initialize(){
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paintOnClick.setAntiAlias(true);
        paintOnClick.setColor(0xCC5E5B5B);
        paint2.setAntiAlias(true);
        paint2.setColor(Color.BLACK);
        paint2.setTextSize(60.0f);
        Paint.Align align = Paint.Align.CENTER;
        paint2.setTextAlign(align);
        isClicked = false;
    }


    public void setClicked(boolean clicked){
        isClicked = clicked;
    }


    //draws the button on the canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth()-startX-1;
        height = getHeight()-startY-1;
        widthBottom  = ((width)*4)/6;
        heightRight  = ((height)*4)/6;
    }


}
