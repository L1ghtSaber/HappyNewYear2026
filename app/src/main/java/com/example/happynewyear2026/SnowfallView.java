package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.*;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class SnowfallView extends View {

    private enum Event {
        DEFAULT, CREATE_NEW, REMOVE_EXPIRED
    }

    private volatile Event currentEvent = Event.DEFAULT;

    private int w, h;

    private Paint snowPaint;
    private ArrayList<Snowflake> snowflakes;
    private CountDownTimer timer;
    private Random random;

    private int maxSnowflakesCount = 30;
    private float maxSnowflakeSize = 40, minSnowflakeSize = maxSnowflakeSize / 4;
    private long timeToCreateSnowflakes = 1000;

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
        random = new Random();

        snowPaint = new Paint();
        snowPaint.setColor(ContextCompat.getColor(context, R.color.dirty_white));

        timer = new CountDownTimer(Long.MAX_VALUE, timeToCreateSnowflakes) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (snowflakes.size() < maxSnowflakesCount) {
                    currentEvent = Event.CREATE_NEW;
                    //invalidate();
                }
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

        maxSnowflakeSize = Math.min(w, h) * 0.025f;
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
        if (expiredSnowflakesIndexes.size() > 0) {
            currentEvent = Event.REMOVE_EXPIRED;
        }

        for (int i = 0; i < snowflakes.size(); i++) {
            Snowflake snowflake = snowflakes.get(i);
            Event event = snowflake.move();
            if (event == Event.REMOVE_EXPIRED) {
                expiredSnowflakesIndexes.add(i);
            }
        }

        invalidate();
    }

    @Override
    public void invalidate() {
        switch (currentEvent) {
            case CREATE_NEW:
                if (snowflakes.size() < maxSnowflakesCount) {
                    generateNewSnowflake();
                }
                currentEvent = Event.DEFAULT;
                break;
            case REMOVE_EXPIRED:
                removeExpiredSnowflakes();
                currentEvent = Event.DEFAULT;
                break;
            case DEFAULT:
            default:
                break;
        }

        super.invalidate();
    }

    private void removeExpiredSnowflakes() {
        if (expiredSnowflakesIndexes.isEmpty()) return;

        expiredSnowflakesIndexes.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Integer.compare(b, a);
            }
        });

        for (int i = 0; i < expiredSnowflakesIndexes.size(); i++) {
            int index = expiredSnowflakesIndexes.get(i);
            if (index >= 0 && index < snowflakes.size()) {
                snowflakes.remove(index);
            }
        }
        expiredSnowflakesIndexes.clear();
    }

    private void generateNewSnowflake() {
        if (w == 0 || h == 0) return;

        float size = random.nextFloat() * (maxSnowflakeSize - minSnowflakeSize) + minSnowflakeSize;
        float y = -size;
        float x = random.nextFloat() * (w + size * 2) - size;

        snowflakes.add(new Snowflake(x, y, size));
    }


    private class Snowflake {
        private float x, y, size;
        private boolean visible;
        private float speedX, speedY;

        public Snowflake(float x, float y, float size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.visible = true;
            this.speedY = 1 + random.nextFloat() * 3;
            this.speedX = (random.nextFloat() - 0.5f) * 2;
        }

        public void draw(Canvas canvas) {
            if (visible) {
                canvas.drawRect(x, y, x + size, y + size, snowPaint);
            }
        }

        public Event move() {
            if (!visible) return Event.DEFAULT;

            y += speedY;
            x += speedX;

            if (y > h + size) {
                visible = false;
                return Event.REMOVE_EXPIRED;
            }

            if (x < -size) {
                x = w + size;
            } else if (x > w + size) {
                x = -size;
            }

            return Event.DEFAULT;
        }
    }

    public void clearSnowflakes() {
        snowflakes.clear();
        expiredSnowflakesIndexes.clear();
        invalidate();
    }

    public void setMaxSnowflakesCount(int count) {
        this.maxSnowflakesCount = Math.max(10, count);
    }

    public void setSnowflakeSize(float maxSize, float minSize) {
        this.maxSnowflakeSize = maxSize;
        this.minSnowflakeSize = minSize;
    }

    public void stopAnimation() {
        if (timer != null) {
            timer.cancel();
        }
    }
}