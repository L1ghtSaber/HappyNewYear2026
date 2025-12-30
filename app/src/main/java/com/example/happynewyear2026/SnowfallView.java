package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.*;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class SnowfallView extends View {

    private enum Event {
        DEFAULT, CREATE_NEW, REMOVE_EXPIRED;
    }

    private volatile Event currentEvent = Event.DEFAULT;

    private int w, h;

    private Paint snowPaint;
    private ArrayList<Snowflake> snowflakes;
    private CountDownTimer timer;

    private int maxSnowflakesCount = 30;
    private float maxSnowflakeSize = 40, minSnowflakeSize = maxSnowflakeSize / 4;
    private long timeToCreateSnowflakes;

    private ArrayList<Integer> expiredSnowflakesIndexes;

    public SnowfallView(Context context) {
        super(context);
        init(context);
    }

    public SnowfallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SnowfallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        snowflakes = new ArrayList<>();
        expiredSnowflakesIndexes = new ArrayList<>();

        snowPaint = new Paint();
        snowPaint.setColor(context.getResources().getColor(R.color.dirty_white));

        timer = new CountDownTimer(1000000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                start();
            }
        }.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.w = w;
        this.h = h;

        maxSnowflakeSize = Math.min(w, h) * 0.1f;
        minSnowflakeSize = maxSnowflakeSize / 4;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < snowflakes.size(); i++) {
            snowflakes.get(i).draw(canvas);
        }

        moveSnowflakes();
    }

    private void moveSnowflakes() {
        for (int i = 0; i < snowflakes.size(); i++) {
            if (snowflakes.get(i).move() == Event.REMOVE_EXPIRED) {
                expiredSnowflakesIndexes.add(i);
            }
        }

        invalidate();
    }

    @Override
    public void invalidate() {
        switch (currentEvent) {
            case CREATE_NEW:
                generateNewSnowflake();
                break;
            case REMOVE_EXPIRED:
                removeExpiredSnowflakes();
                break;
            case DEFAULT:
            default:
        }

        super.invalidate();
    }

    private void removeExpiredSnowflakes() {
        for (int i = 0; i < expiredSnowflakesIndexes.size(); i++) {
            snowflakes.remove(expiredSnowflakesIndexes.get(i));
        }
    }

    private void generateNewSnowflake() {
        float size = (float) Math.random() * (maxSnowflakeSize - minSnowflakeSize) + minSnowflakeSize;
        float y = -size, x = (float) Math.random() * (w + size * 1.5f) - size / 2;

        snowflakes.add(new Snowflake(x, y, size));
    }

    private class Snowflake {
        private float x, y, size;
        private boolean visible;

        public Snowflake(float x, float y, float size) {
            this.x = x;
            this.y = y;
            this.size = size;
            visible = true;
        }

        public void draw(Canvas canvas) {
            if (visible)
                canvas.drawRect(x, y, x + size, y + size, snowPaint);
        }

        public Event move() {
            x += (float) Math.random() * size * ((int) (Math.random() * 2) == 0 ? 1 : -1);
            y += (float) Math.random() * size;

            return Event.DEFAULT;
        }
    }
}

