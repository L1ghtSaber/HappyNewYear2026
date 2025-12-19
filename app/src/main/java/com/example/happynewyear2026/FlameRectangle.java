package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FlameRectangle extends View {

    private Paint primaryPaint, secondaryPaint;
    private RectF primaryRect;
    private float secondaryRectSide, secondaryRectSideFactor = 0.05f, secondaryRectSpeed = 10f, rectGap;
    private ArrayList<SecondaryRect> secondaryRects;

    private enum Position {
        LEFT, TOP, RIGHT, BOTTOM;
    }

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
        secondaryRects = new ArrayList<>();

        primaryPaint = new Paint();
        primaryPaint.setColor(Color.RED);
        primaryPaint.setStyle(Paint.Style.FILL);

        secondaryPaint = new Paint();
        secondaryPaint.setColor(Color.BLUE);
        secondaryPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        secondaryRectSide = secondaryRectSideFactor * Math.min(w, h);
        primaryRect = new RectF(secondaryRectSide, secondaryRectSide, w - secondaryRectSide, h - secondaryRectSide);

        float x = primaryRect.left - secondaryRectSide, y = primaryRect.bottom - secondaryRectSide, oldX, oldY;
        Position position = Position.LEFT;
        float perimeter = primaryRect.width() * 2 + primaryRect.height() * 2;
        int amount = (int) Math.floor(perimeter / (2 * secondaryRectSide));
        rectGap = (perimeter - amount * 2 * secondaryRectSide) / amount + secondaryRectSide;

        THIS: while (true) {
            SecondaryRect rect = new SecondaryRect(x, y, position);
            secondaryRects.add(rect);
            oldX = x;
            oldY = y;
            switch (position) {
                case LEFT:
                    y -= rectGap + secondaryRectSide;
                    if (y <= primaryRect.top) {
                        position = Position.TOP;
                        y = primaryRect.top - secondaryRectSide;
                        x = primaryRect.left;
                        //if (oldY - y < rectGap) secondaryRects.remove(rect);
                    }
                    break;
                case TOP:
                    x += rectGap + secondaryRectSide;
                    if (x + secondaryRectSide >= primaryRect.right) {
                        position = Position.RIGHT;
                        x = primaryRect.right;
                        y = primaryRect.top;
                        //if (x - oldX < rectGap) secondaryRects.remove(rect);
                    }
                    break;
                case RIGHT:
                    y += rectGap + secondaryRectSide;
                    if (y + secondaryRectSide >= primaryRect.bottom) {
                        position = Position.BOTTOM;
                        y = primaryRect.bottom;
                        x = primaryRect.right - secondaryRectSide;
                        //if (y - oldY < rectGap) secondaryRects.remove(rect);
                    }
                    break;
                case BOTTOM:
                    x -= rectGap + secondaryRectSide;
                    if (x <= primaryRect.left) {
                        //if (oldX - x < rectGap) secondaryRects.remove(rect);
                        SecondaryRect first = secondaryRects.get(0);
                        //if (Math.sqrt(Math.pow(oldX - first.getX(), 2) + Math.pow(oldY - first.getY(), 2)) < rectGap) secondaryRects.remove(rect);
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

    void moveRects() {
        for (int i = 0; i < secondaryRects.size(); i++) {
            secondaryRects.get(i).move();
        }

        invalidate();
    }

    private class SecondaryRect {
        private Position position;
        private float x, y;

        SecondaryRect(float x, float y, Position position) {
            this.position = position;
            this.x = x;
            this.y = y;
        }

        void move() {
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

        void draw(Canvas canvas) {
            canvas.drawRect(x, y, x + secondaryRectSide, y + secondaryRectSide, secondaryPaint);
        }

        Position getPosition() {
            return position;
        }

        float getX() {
            return x;
        }

        float getY() {
            return y;
        }
    }

    //    private void init() {
//        rcPaint = new Paint();
//        rcPaint.setColor(rcColor);
//        rcPaint.setStyle(Paint.Style.FILL);
//
//        trPaint = new Paint();
//        trPaint.setColor(trColor);
//        trPaint.setStyle(Paint.Style.FILL);
//
//        triangle = new Path();
//
//        trMatrix = new Matrix();
//    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//
//        trSide = trSideFactor * Math.min(w, h);
//        rectangle = new RectF(trSide, trSide, w - trSide, h - trSide);
//        perimeter = rectangle.width() * 2 + rectangle.height() * 2;
//        trCount = (int) (perimeter / trSide);
//        trGap = (perimeter - trCount * trSide) / trCount;
//        rcCornerRadius = trSide / 2;
//
//        points = new float[trCount][3];
//        points[0][0] = rectangle.left;
//        points[0][1] = rectangle.top + rcCornerRadius + trSide / 2;
//        points[0][2] = 90;
//        for (int i = 1; i < trCount; i++)
//            points[i] = getNextPoint(points[i-1], true);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        canvas.drawRect(rectangle, rcPaint);
//
//        for (int i = 0; i < trCount; i++) {
//            float x = points[i][0], y = points[i][1];
//            points[i] = getNextPoint(points[i], false);
//
//            triangle.reset();
//            triangle.moveTo(0, trSide * 0.866f);
//            triangle.lineTo(-trSide / 2, 0);
//            triangle.lineTo(trSide / 2, 0);
//            triangle.close();
//
//            trMatrix.reset();
//            trMatrix.postRotate(points[i][2]);
//            trMatrix.postTranslate(x, y);
//            triangle.transform(trMatrix);
//
//            canvas.drawPath(triangle, trPaint);
//        }
//
//        if (trSpeed != 0) invalidate();
//    }
//
//    private TrianglePosition getPointPosition(float x, float y) {
//        float t = 2f;
//
//        if (Math.abs(x - rectangle.left) < t &&
//                y > rectangle.top + rcCornerRadius &&
//                y <= rectangle.bottom - rcCornerRadius)
//            return TrianglePosition.LEFT;
//
//        if (Math.abs(y - rectangle.top) < t &&
//                x > rectangle.left + rcCornerRadius &&
//                x <= rectangle.right - rcCornerRadius)
//            return TrianglePosition.TOP;
//
//        if (Math.abs(x - rectangle.right) < t &&
//                y > rectangle.top + rcCornerRadius &&
//                y <= rectangle.bottom - rcCornerRadius)
//            return TrianglePosition.RIGHT;
//
//        if (Math.abs(y - rectangle.bottom) < t &&
//                x > rectangle.left + rcCornerRadius &&
//                x <= rectangle.right - rcCornerRadius)
//            return TrianglePosition.BOTTOM;
//
//        if (x >= rectangle.left && x <= rectangle.left + rcCornerRadius &&
//                y >= rectangle.top && y <= rectangle.top + rcCornerRadius)
//            return TrianglePosition.LT_CORNER;
//
//        if (x >= rectangle.right - rcCornerRadius && x <= rectangle.right &&
//                y >= rectangle.top && y <= rectangle.top + rcCornerRadius)
//            return TrianglePosition.RT_CORNER;
//
//        if (x >= rectangle.right - rcCornerRadius && x <= rectangle.right &&
//                y >= rectangle.bottom - rcCornerRadius && y <= rectangle.bottom)
//            return TrianglePosition.RB_CORNER;
//
//        if (x >= rectangle.left && x <= rectangle.left + rcCornerRadius &&
//                y >= rectangle.bottom - rcCornerRadius && y <= rectangle.bottom)
//            return TrianglePosition.LB_CORNER;
//
//        return TrianglePosition.LEFT;
//    }
//
//    private float[] getNextPoint(float[] point, boolean initial) {
//        TrianglePosition position = getPointPosition(point[0], point[1]);
//        float x = point[0], y = point[1], rotationAngle = point[2],
//                moveDistance = initial ? (trSide + trGap) : trSpeed,
//                angle = (float) (Math.toRadians(rotationAngle) + Math.PI / 2),
//                mDx = 0, mDy = 0;
//
//        switch (position) {
//            case LEFT:
//                y += moveDistance;
//                rotationAngle = 90;
//                break;
//            case TOP:
//                x += moveDistance;
//                rotationAngle = 0;
//                break;
//            case RIGHT:
//                y -= moveDistance;
//                rotationAngle = 270;
//                break;
//            case BOTTOM:
//                x -= moveDistance;
//                rotationAngle = 180;
//                break;
//            case LT_CORNER:
//            case RT_CORNER:
//            case RB_CORNER:
//            case LB_CORNER:
////                mDx = rcCornerRadius * (float) Math.cos(Math.toRadians(rotationAngle));
////                mDy = rcCornerRadius * (float) Math.sin(Math.toRadians(rotationAngle));
//                mDx = trSpeed;
//                mDy = trSpeed;
//                break;
//        }
//
//        switch (position) {
//            case LT_CORNER:
//                x -= mDx;
//                y += mDy;
//                rotationAngle += (float) Math.toDegrees(trSpeed / rcCornerRadius);
//                break;
//            case RB_CORNER:
//                x += mDx;
//                y -= mDy;
//                rotationAngle -= (float) Math.toDegrees(trSpeed / rcCornerRadius);
//                break;
//            case RT_CORNER:
//                x -= mDx;
//                y -= mDy;
//                rotationAngle += (float) Math.toDegrees(trSpeed / rcCornerRadius);
//                break;
//            case LB_CORNER:
//                x += mDx;
//                y += mDy;
//                rotationAngle -= (float) Math.toDegrees(trSpeed / rcCornerRadius);
//                break;
//        }
//
//        return new float[]{x, y, rotationAngle};
//    }
//
//    private static class Triangle {
//        private enum Position {
//            LEFT, TOP, RIGHT, BOTTOM,
//            LT_CORNER, RT_CORNER, RB_CORNER, LB_CORNER;
//        }
//
//        private static Matrix matrix;
//        private static Path body;
//        private static Paint paint;
//        private static float sideFactor, side, speed, gap;
//
//        private float x, y, rotationAngle;
//        private Position position;
//    }
//
//    private static class Rectangle {
//        private static Paint paint;
//
//        private RectF body;
//        private float perimeter, cornerRadius;
//
//        Rectangle()
//    }
}
