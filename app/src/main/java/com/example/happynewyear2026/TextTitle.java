package com.example.happynewyear2026;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.icu.text.CaseMap;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class TextTitle {
    private Context context;
    private String text;
    private String[] textParts;
    private RectF bounds;
    private Paint paint;
    private float startX, startY, textWidth, textHeight;

    public TextTitle(Context context, String[] textParts, RectF bounds, int color, float size) {
        this.context = context;
        this.textParts = textParts;
        this.bounds = bounds;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1);
        paint.setTypeface(ResourcesCompat.getFont(context, R.font.font_8_bit));
        paint.setTextSize(size);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(color);

        concatTextParts();
        alignText(true);
    }

    private void concatTextParts() {
        text = String.join("", textParts);
    }

    public TextTitle alignText(boolean setTextWidthAndHeight) {
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);

        startX = bounds.centerX() - textBounds.width() / 2f;
        startY = bounds.centerY() + textBounds.height() / 2f;

        if (setTextWidthAndHeight) setTextWidthAndHeight();

        return this;
    }

    private void setTextWidthAndHeight() {
        textWidth = paint.measureText(text);
        textHeight = paint.getFontMetrics().bottom - paint.getFontMetrics().top;
    }

    public TextTitle setTextPart(int index, String textPart) {
        textParts[index] = textPart;
        concatTextParts();
        alignText(false);

        return this;
    }

    public TextTitle setTextParts(String[] textParts) {
        this.textParts = textParts;
        concatTextParts();
        alignText(false);

        return this;
    }

    public void drawWithoutLines(Canvas canvas) {
        canvas.drawText(text, startX, startY, paint);
    }

    public void drawWithLines(Canvas canvas) {
        setTextWidthAndHeight();

        String[] lines = text.split("\n");
        float indent = (bounds.height() - textHeight * lines.length) / 2;
        for (int i = 0; i < lines.length; i++) {
            TextTitle title = new TextTitle(context, new String[]{lines[i]},
                    new RectF(bounds.left, bounds.top + indent + i * textHeight,
                            bounds.right, bounds.bottom - indent - (lines.length - i - 1) * textHeight),
                    paint.getColor(), paint.getTextSize());

            title.drawWithoutLines(canvas);
        }
    }
}
