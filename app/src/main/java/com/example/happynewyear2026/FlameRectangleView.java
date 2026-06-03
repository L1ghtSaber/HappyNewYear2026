package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class FlameRectangleView extends View {

    private static final float DEFAULT_SIDE_FACTOR = 0.05f, DEFAULT_RECT_SPEED = 10f;

    private Paint primaryPaint, secondaryPaint;
    private RectF primaryRect;
    private Context context;
    private float secondaryRectSide, secondaryRectSideFactor = DEFAULT_SIDE_FACTOR, secondaryRectSpeed = DEFAULT_RECT_SPEED;
    private ArrayList<SecondaryRect> secondaryRects;

    private enum Position {
        LEFT, TOP, RIGHT, BOTTOM;
    }

    private int w, h;

    public FlameRectangleView(Context context) {
        super(context);
        init(context);
    }

    public FlameRectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlameRectangleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;

        secondaryRects = new ArrayList<>();

        primaryPaint = new Paint();
        primaryPaint.setStyle(Paint.Style.FILL);

        secondaryPaint = new Paint();
        secondaryPaint.setStyle(Paint.Style.FILL);
        reset();
    }

    public void reset() {
        secondaryRectSideFactor = DEFAULT_SIDE_FACTOR;
        secondaryRectSpeed = DEFAULT_RECT_SPEED;
        setColors(ContextCompat.getColor(context, R.color.primary), ContextCompat.getColor(context, R.color.secondary));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.w = w;
        this.h = h;

        calculateRects();
    }

    private void calculateRects() {
        secondaryRectSide = secondaryRectSideFactor * Math.min(w, h);
        primaryRect = new RectF(secondaryRectSide, secondaryRectSide, w - secondaryRectSide, h - secondaryRectSide);

        float x = primaryRect.left - secondaryRectSide, y = primaryRect.bottom - secondaryRectSide;
        Position position = Position.LEFT;
        //float perimeter = primaryRect.width() * 2 + primaryRect.height() * 2;
        //int amount = (int) Math.floor(perimeter / (2 * secondaryRectSide));
        //float rectGap = (perimeter - amount * 2 * secondaryRectSide) / amount + secondaryRectSide;


        THIS: while (true) {
            secondaryRects.add(new SecondaryRect(x, y, position));

            float rectGap = (float) Math.random() * secondaryRectSide * 2 + secondaryRectSide * 2;

            switch (position) {
                case LEFT:
                    y -= rectGap + secondaryRectSide;
                    if (y <= primaryRect.top) {
                        position = Position.TOP;
                        float y1 = y;
                        y = primaryRect.top - secondaryRectSide;
                        x = primaryRect.left - secondaryRectSide + y - y1;
                    }
                    break;
                case TOP:
                    x += rectGap + secondaryRectSide;
                    if (x + secondaryRectSide >= primaryRect.right) {
                        position = Position.RIGHT;
                        float x1 = x;
                        x = primaryRect.right;
                        y = primaryRect.top - secondaryRectSide + x1 - x;
                    }
                    break;
                case RIGHT:
                    y += rectGap + secondaryRectSide;
                    if (y + secondaryRectSide >= primaryRect.bottom) {
                        position = Position.BOTTOM;
                        float y1 = y;
                        y = primaryRect.bottom;
                        x = primaryRect.right - (y - y1);
                    }
                    break;
                case BOTTOM:
                    x -= rectGap + secondaryRectSide;
                    if (x <= primaryRect.left) {
                        break THIS;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(primaryRect, primaryPaint);

        for (int i = 0; i < secondaryRects.size(); i++) {
            secondaryRects.get(i).draw(canvas);
        }

        if (secondaryRectSpeed != 0) moveRects();
    }

    private void moveRects() {
        for (int i = 0; i < secondaryRects.size(); i++) {
            secondaryRects.get(i).move();
        }

        invalidate();
    }

    public void setSecondaryRectSpeed(float speed) {
        this.secondaryRectSpeed = speed;
        invalidate();
    }

    public void setSecondaryRectSideFactor(float factor) {
        if (factor != this.secondaryRectSideFactor) {
            this.secondaryRectSideFactor = Math.max(0.01f, Math.min(factor, 0.3f));
            calculateRects();
            invalidate();
        }
    }

    public void setColors(int primary, int secondary) {
        primaryPaint.setColor(primary);
        secondaryPaint.setColor(secondary);
        invalidate();
    }

    private class SecondaryRect {
        private Position position;
        private float x, y;

        public SecondaryRect(float x, float y, Position position) {
            this.position = position;
            this.x = x;
            this.y = y;
        }

        public void move() {
            switch (position) {
                case LEFT:
                case RIGHT:
                    y += secondaryRectSpeed * (position == Position.LEFT ? -1 : 1);
                    if (y + secondaryRectSide < primaryRect.top) {
                        y = primaryRect.top - secondaryRectSide;
                        position = Position.TOP;
                    }
                    if (y > primaryRect.bottom) {
                        y = primaryRect.bottom;
                        position = Position.BOTTOM;
                    }
                    break;
                case TOP:
                case BOTTOM:
                    x += secondaryRectSpeed * (position == Position.TOP ? 1 : -1);
                    if (x + secondaryRectSide < primaryRect.left) {
                        x = primaryRect.left - secondaryRectSide;
                        position = Position.LEFT;
                    }
                    if (x > primaryRect.right) {
                        x = primaryRect.right;
                        position = Position.RIGHT;
                    }
                    break;
            }
        }

        public void draw(Canvas canvas) {
            canvas.drawRect(x, y, x + secondaryRectSide, y + secondaryRectSide, secondaryPaint);
        }

        public Position getPosition() {
            return position;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }
}
