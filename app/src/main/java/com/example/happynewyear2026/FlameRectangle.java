package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class FlameRectangle extends View {
    private Paint rcPaint, trPaint;
    private RectF rectangle;

    private int rcColor = Color.RED, trColor = Color.BLUE,
            trCount;
    private float trSideFactor = 0.1f,
            trSide, perimeter, trGap;

    public FlameRectangle(Context context) {
        super(context);
        init();
    }

    public FlameRectangle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlameRectangle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rcPaint = new Paint();
        rcPaint.setColor(rcColor);
        rcPaint.setStyle(Paint.Style.FILL);

        trPaint = new Paint();
        trPaint.setColor(trColor);
        trPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        trSide = trSideFactor * Math.min(w, h);
        rectangle = new RectF(trSide, trSide, w - trSide, h - trSide);
        perimeter = rectangle.width() * 2 + rectangle.height() * 2;
        trCount = (int) (perimeter / trSide);
        trGap = (perimeter - trCount * trSide) / trCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int a;
    }
}
