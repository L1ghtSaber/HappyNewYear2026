package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.core.content.res.ResourcesCompat;

public class TextTitle {

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    private final Context context;
    private String text;
    private String[] textParts;
    private RectF bounds, textRect;
    private final Paint paint, inversePaint;
    private float startX, startY, textWidth, textHeight;
    private Alignment alignment;
    private boolean inverse;
    private float paddingX = 10f;
    private float paddingY = 10f;

    public TextTitle(Context context, String[] textParts, RectF bounds,
                     int color, float size, Alignment alignment) {
        this.context = context;
        this.textParts = textParts;
        this.alignment = alignment;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1);
        paint.setTypeface(ResourcesCompat.getFont(context, R.font.font_8_bit));
        paint.setTextSize(size);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(color);

        inversePaint = new Paint(paint);
        inversePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        textRect = new RectF();

        concatTextParts();
        setBounds(bounds);
    }

    public TextTitle(Context context, String[] textParts, RectF bounds,
                     int color, float size) {
        this(context, textParts, bounds, color, size, Alignment.CENTER);
    }

    private void concatTextParts() {
        text = String.join("", textParts);
    }

    public void setBounds(RectF bounds) {
        this.bounds = bounds;
        alignText(true);
        updateTextRect();
    }

    public RectF getBounds() {
        return bounds;
    }

    public RectF getTextRect() {
        return textRect;
    }

    public void setPadding(float paddingX, float paddingY) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        updateTextRect();
    }

    public boolean getInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public TextTitle alignText(boolean setTextWidthAndHeight) {
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);

        switch (alignment) {
            case LEFT:
                startX = bounds.left;
                break;
            case RIGHT:
                startX = bounds.right - textBounds.width();
                break;
            case CENTER:
            default:
                startX = bounds.centerX() - textBounds.width() / 2f;
                break;
        }
        startY = bounds.centerY() + textBounds.height() / 2f;

        if (setTextWidthAndHeight) setTextWidthAndHeight();
        return this;
    }

    private void setTextWidthAndHeight() {
        textWidth = paint.measureText(text);
        textHeight = paint.getFontMetrics().bottom - paint.getFontMetrics().top;
    }

    private void updateTextRect() {
        if (text == null || text.isEmpty()) {
            textRect.setEmpty();
            return;
        }
        String[] lines = text.split("\n");
        if (lines.length == 0) {
            textRect.setEmpty();
            return;
        }

        float maxWidth = 0;
        for (int i = 0; i < lines.length; i++) {
            float w = paint.measureText(lines[i]);
            if (w > maxWidth) maxWidth = w;
        }
        float totalHeight = lines.length * textHeight;

        float left, top, right, bottom;
        switch (alignment) {
            case LEFT:
                left = bounds.left;
                break;
            case RIGHT:
                left = bounds.right - maxWidth;
                break;
            default:
                left = bounds.centerX() - maxWidth / 2f;
                break;
        }
        top = bounds.centerY() - totalHeight / 2f;

        left -= paddingX;
        top -= paddingY;
        right = left + maxWidth + 2 * paddingX;
        bottom = top + totalHeight + 2 * paddingY;

        textRect.set(left, top, right, bottom);
    }

    public TextTitle setTextPart(int index, String textPart) {
        textParts[index] = textPart;
        concatTextParts();
        alignText(false);
        updateTextRect();
        return this;
    }

    public TextTitle setTextParts(String[] textParts) {
        this.textParts = textParts;
        concatTextParts();
        alignText(false);
        updateTextRect();
        return this;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
        alignText(false);
        updateTextRect();
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void drawWithoutLines(Canvas canvas) {
        if (inverse) {
            int saveCount = canvas.saveLayer(textRect.left, textRect.top,
                    textRect.right, textRect.bottom, null);
            canvas.drawRect(textRect, paint);
            canvas.drawText(text, startX, startY, inversePaint);
            canvas.restoreToCount(saveCount);
        } else {
            canvas.drawText(text, startX, startY, paint);
        }
    }

    public void drawWithLines(Canvas canvas) {
        if (text == null || text.isEmpty()) return;

        String[] lines = text.split("\n");
        if (lines.length == 0) return;

        float totalHeight = lines.length * textHeight;
        float startYOffset = bounds.centerY() - totalHeight / 2f;

        if (inverse) {
            RectF totalTextRect = new RectF();
            float maxWidth = 0;
            for (String line : lines) {
                float w = paint.measureText(line);
                if (w > maxWidth) maxWidth = w;
            }
            float left, top, right, bottom;
            switch (alignment) {
                case LEFT:   left = bounds.left; break;
                case RIGHT:  left = bounds.right - maxWidth; break;
                default:     left = bounds.centerX() - maxWidth / 2f; break;
            }
            top = startYOffset;
            left -= paddingX;
            top -= paddingY;
            right = left + maxWidth + 2 * paddingX;
            bottom = top + totalHeight + 2 * paddingY;
            totalTextRect.set(left, top, right, bottom);

            int saveCount = canvas.saveLayer(totalTextRect.left, totalTextRect.top,
                    totalTextRect.right, totalTextRect.bottom, null);
            canvas.drawRect(totalTextRect, paint);

            for (int i = 0; i < lines.length; i++) {
                float lineWidth = paint.measureText(lines[i]);
                float lineStartX;
                switch (alignment) {
                    case LEFT:   lineStartX = bounds.left; break;
                    case RIGHT:  lineStartX = bounds.right - lineWidth; break;
                    default:     lineStartX = bounds.centerX() - lineWidth / 2f; break;
                }
                float lineY = startYOffset + i * textHeight + textHeight * 0.85f;
                canvas.drawText(lines[i], lineStartX, lineY, inversePaint);
            }
            canvas.restoreToCount(saveCount);
        } else {
            for (int i = 0; i < lines.length; i++) {
                float lineWidth = paint.measureText(lines[i]);
                float lineStartX;
                switch (alignment) {
                    case LEFT:   lineStartX = bounds.left; break;
                    case RIGHT:  lineStartX = bounds.right - lineWidth; break;
                    default:     lineStartX = bounds.centerX() - lineWidth / 2f; break;
                }
                float lineY = startYOffset + i * textHeight + textHeight * 0.85f;
                canvas.drawText(lines[i], lineStartX, lineY, paint);
            }
        }
    }

//    public void drawWithLines(Canvas canvas) {
//        setTextWidthAndHeight();
//        String[] lines = text.split("\n");
//        float indent = (bounds.height() - textHeight * lines.length) / 2;
//
//        for (int i = 0; i < lines.length; i++) {
//            RectF lineBounds = new RectF(
//                    bounds.left,
//                    bounds.top + indent + i * textHeight,
//                    bounds.right,
//                    bounds.bottom - indent - (lines.length - i - 1) * textHeight
//            );
//            TextTitle title = new TextTitle(
//                    context,
//                    new String[]{lines[i]},
//                    lineBounds,
//                    paint.getColor(),
//                    paint.getTextSize(),
//                    alignment
//            );
//            title.setInverse(true);
//            title.drawWithoutLines(canvas);
//        }
//    }
}