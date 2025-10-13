package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class FlameRectangle extends View {
    private Paint rcPaint, trPaint;
    private RectF rectangle;
    private Path triangle;
    private Matrix trMatrix;

    private int rcColor = Color.RED, trColor = Color.BLUE,
            trCount;
    private float trSideFactor = 0.1f,
            trSide, perimeter, trGap, trSpeed = 1f,
            rcCornerRadius;
    private float[][] points;

    private enum TrianglePosition {
        LEFT, TOP, RIGHT, BOTTOM,
        LT_CORNER, RT_CORNER, RB_CORNER, LB_CORNER;
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
        rcPaint = new Paint();
        rcPaint.setColor(rcColor);
        rcPaint.setStyle(Paint.Style.FILL);

        trPaint = new Paint();
        trPaint.setColor(trColor);
        trPaint.setStyle(Paint.Style.FILL);

        triangle = new Path();

        trMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        trSide = trSideFactor * Math.min(w, h);
        rectangle = new RectF(trSide, trSide, w - trSide, h - trSide);
        perimeter = rectangle.width() * 2 + rectangle.height() * 2;
        trCount = (int) (perimeter / trSide);
        trGap = (perimeter - trCount * trSide) / trCount;
        rcCornerRadius = trSide / 2;

        points = new float[trCount][3];
        points[0][0] = rectangle.left;
        points[0][1] = rectangle.top + rcCornerRadius + trSide / 2;
        points[0][2] = 90;
        for (int i = 1; i < trCount; i++)
            points[i] = getNextPoint(points[i-1][0], points[i-1][1], true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(rectangle, rcPaint);

        for (int i = 0; i < trCount; i++) {
            float x = points[i][0], y = points[i][1];
            points[i] = getNextPoint(x, y, false);

            triangle.reset();
            triangle.moveTo(0, trSide * 0.866f);
            triangle.lineTo(-trSide / 2, 0);
            triangle.lineTo(trSide / 2, 0);
            triangle.close();

            trMatrix.reset();
            trMatrix.postRotate(points[i][2]);
            trMatrix.postTranslate(x, y);
            triangle.transform(trMatrix);

            canvas.drawPath(triangle, trPaint);
        }

        if (trSpeed != 0) invalidate();
    }

    private TrianglePosition getPointPosition(float x, float y) {
        if (Math.abs(x - rectangle.left) < 1 &&
                y > rectangle.top + rcCornerRadius &&
                y <= rectangle.bottom - rcCornerRadius)
            return TrianglePosition.LEFT;

        if (Math.abs(y - rectangle.top) < 1 &&
                x > rectangle.left + rcCornerRadius &&
                x <= rectangle.right - rcCornerRadius)
            return TrianglePosition.TOP;

        if (Math.abs(x - rectangle.right) < 1 &&
                y > rectangle.top + rcCornerRadius &&
                y <= rectangle.bottom - rcCornerRadius)
            return TrianglePosition.RIGHT;

        if (Math.abs(y - rectangle.bottom) < 1 &&
                x > rectangle.left + rcCornerRadius &&
                x <= rectangle.right - rcCornerRadius)
            return TrianglePosition.BOTTOM;

        if (x >= rectangle.left && x <= rectangle.left + rcCornerRadius &&
                y >= rectangle.top && y <= rectangle.top + rcCornerRadius)
            return TrianglePosition.LT_CORNER;

        if (x >= rectangle.right - rcCornerRadius && x <= rectangle.right &&
                y >= rectangle.top && y <= rectangle.top + rcCornerRadius)
            return TrianglePosition.RT_CORNER;

        if (x >= rectangle.right - rcCornerRadius && x <= rectangle.right &&
                y >= rectangle.bottom - rcCornerRadius && y <= rectangle.bottom)
            return TrianglePosition.RB_CORNER;

        if (x >= rectangle.left && x <= rectangle.left + rcCornerRadius &&
                y >= rectangle.bottom - rcCornerRadius && y <= rectangle.bottom)
            return TrianglePosition.LB_CORNER;

        return TrianglePosition.LEFT;
    }

    private float[] getNextPoint(float startX, float startY, boolean initial) {
        TrianglePosition position = getPointPosition(startX, startY);
        float x = startX, y = startY;
        float rotationAngle = 0;
        float moveDistance = initial ? (trSide + trGap) : trSpeed;

        switch (position) {
            case LEFT:
                y += moveDistance;
                rotationAngle = 90;
                if (y > rectangle.bottom - rcCornerRadius) {
                    y = rectangle.bottom - rcCornerRadius;
                    x = rectangle.left + y - (rectangle.bottom - rcCornerRadius);
                }
                break;
                //...
            case TOP:
                x += moveDistance;
                rotationAngle = 0;
                if (x > rectangle.right - rcCornerRadius) {
                    x = rectangle.right - rcCornerRadius;
                    y = rectangle.top + x - (rectangle.right - rcCornerRadius);
                }
                break;

            case RIGHT:
                y -= moveDistance;
                rotationAngle = 270;
                if (y < rectangle.top + rcCornerRadius) {
                    y = rectangle.top + rcCornerRadius;
                    x = rectangle.right - (rectangle.top + rcCornerRadius) - y;
                }
                break;

            case BOTTOM:
                x -= moveDistance;
                rotationAngle = 180;
                if (x < rectangle.left + rcCornerRadius) {
                    x = rectangle.left + rcCornerRadius;
                    y = rectangle.bottom - (rectangle.left + rcCornerRadius) - x;
                }
                break;

            case LT_CORNER:
                float angleLT = (float) Math.atan2(startY - rectangle.top, startX - rectangle.left);
                angleLT += moveDistance / rcCornerRadius;
                x = rectangle.left + (float) (rcCornerRadius * Math.cos(angleLT));
                y = rectangle.top + (float) (rcCornerRadius * Math.sin(angleLT));
                rotationAngle = angleLT * 180 / (float) Math.PI + 90;
                if (angleLT >= Math.PI / 2) {
                    x = rectangle.left;
                    y = rectangle.top + rcCornerRadius;
                }
                break;

            case RT_CORNER:
                float angleRT = (float) Math.atan2(startY - rectangle.top, startX - rectangle.right);
                angleRT += moveDistance / rcCornerRadius;
                x = rectangle.right + (float) (rcCornerRadius * Math.cos(angleRT));
                y = rectangle.top + (float) (rcCornerRadius * Math.sin(angleRT));
                rotationAngle = angleRT * 180 / (float) Math.PI + 180;
                if (angleRT >= Math.PI / 2) {
                    x = rectangle.right - rcCornerRadius;
                    y = rectangle.top;
                }
                break;

            case RB_CORNER:
                float angleRB = (float) Math.atan2(startY - rectangle.bottom, startX - rectangle.right);
                angleRB += moveDistance / rcCornerRadius;
                x = rectangle.right + (float) (rcCornerRadius * Math.cos(angleRB));
                y = rectangle.bottom + (float) (rcCornerRadius * Math.sin(angleRB));
                rotationAngle = angleRB * 180 / (float) Math.PI + 270;
                if (angleRB >= Math.PI / 2) {
                    x = rectangle.right;
                    y = rectangle.bottom - rcCornerRadius;
                }
                break;

            case LB_CORNER:
                float angleLB = (float) Math.atan2(startY - rectangle.bottom, startX - rectangle.left);
                angleLB += moveDistance / rcCornerRadius;
                x = rectangle.left + (float) (rcCornerRadius * Math.cos(angleLB));
                y = rectangle.bottom + (float) (rcCornerRadius * Math.sin(angleLB));
                rotationAngle = angleLB * 180 / (float) Math.PI;
                if (angleLB >= Math.PI / 2) {
                    x = rectangle.left + rcCornerRadius;
                    y = rectangle.bottom;
                }
                break;
        }

        return new float[]{x, y, rotationAngle};
    }
}
