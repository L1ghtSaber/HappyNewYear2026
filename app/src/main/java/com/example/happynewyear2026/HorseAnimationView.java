package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Random;

public class HorseAnimationView extends View {
    private Entity.Horse horse;
    private float horseSpeed = 4f;
    private final float DEFAULT_X = -2000;

    private ArrayList<Entity.Snowdrift> snowdrifts;
    private int snowdriftCount = 10;
    private float groundLevel;

    private TextTitle title;
    private Paint bubblePaint;
    private String displayText = "С Новым годом!";
    private RectF textBubble;
    private int textPadding = 20;

    private Paint groundPaint;

    private boolean isAnimating = true;
    private long lastUpdateTime;

    private int w, h;

    public HorseAnimationView(Context context) {
        super(context);
        init(context);
    }

    public HorseAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HorseAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setColor(ContextCompat.getColor(context, R.color.white));
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setStrokeWidth(2);

        groundPaint = new Paint();
        groundPaint.setColor(ContextCompat.getColor(context, R.color.dirty_white));
        groundPaint.setStyle(Paint.Style.FILL);

        textBubble = new RectF();

        setClickable(false);
        setFocusable(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.w = w;
        this.h = h;

        groundLevel = h * 0.8f;

        horse = new Entity.Horse(
                DEFAULT_X,
                groundLevel,
                getContext()
        );
        horse.setUpdatable(false);
        horse.setTag(Entity.Tag.IRREMOVABLE, true);
        setTextBubble();

        horse.setSpeedX(horseSpeed);
        horse.setSpeedY(0);

        createSnowdrifts();

        title = new TextTitle(getContext(), new String[]{getDateInsertion()}, textBubble, ContextCompat.getColor(getContext(), R.color.black), 30);

        lastUpdateTime = System.currentTimeMillis();
    }

    private void createSnowdrifts() {
        int horseWidth = horse.getFrameWidth(), horseHeight = horse.getFrameHeight();

        snowdrifts = Entity.Snowdrift.generateSnowdrifts(getContext(), snowdriftCount, w, groundLevel,
                horseWidth / 2, horseWidth,
                0, horseHeight / 8);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, groundLevel, w, h, groundPaint);

        horse.draw(canvas);

        for (int i = 0; i < snowdriftCount; i++) snowdrifts.get(i).draw(canvas);

        drawTextBubble(canvas);

        if (isAnimating) {
            updateAnimation();
            invalidate();
        }
    }

    private String getDateInsertion() {
        String insertion;
        if (Build.VERSION.SDK_INT >= 26) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
            if (now.isBefore(LocalDateTime.of(2026, 1, 1, 0, 0))) {
                int hour = 23 - now.getHour(), minute = 60 - now.getMinute();
                if (minute == 60) {
                    hour++;
                    minute = 0;
                }
                insertion = "ДО НОВОГО-ГО ИГО-ГОДА\nОСТАЛОСЬ:\n";
                if (hour != 0) {
                    String word = "ЧАС";
                    if (hour >= 2 && hour <= 4 || (hour > 20 && hour % 10 >= 2 && hour % 10 <= 4))
                        word += "А";
                    else if (hour >= 5 && hour <= 19 || (hour >= 20 && (hour % 10 == 0 || hour % 10 >= 5)))
                        word += "ОВ";
                    insertion += " " + hour + " " + word;
                }
                if (minute != 0) {
                    String word = "МИНУТ";
                    if (minute == 1 || (minute > 20 && minute % 10 == 1))
                        word += "A";
                    else if (minute >= 2 && minute <= 4 || (minute > 20 && minute % 10 >= 2 && minute % 10 <= 4))
                        word += "Ы";
                    insertion += " " + minute + " " + word;
                }
                insertion += "";
            } else insertion = "";
        } else insertion = "";

        if (!insertion.isEmpty()) return insertion;

        return "С НАСТУПИВШИМ НОВЫМ\n2026 ИГО-ГО-ГОДОМ!";
    }

    private void drawTextBubble(Canvas canvas) {
        canvas.drawRect(textBubble, bubblePaint);

        Path tail = new Path();
        tail.moveTo(textBubble.left, textBubble.bottom);
        tail.lineTo(textBubble.left, textBubble.bottom + textBubble.height() / 8);
        tail.lineTo(textBubble.left + textBubble.height() / 8, textBubble.bottom);
        tail.close();

        canvas.drawPath(tail, bubblePaint);

        title.drawWithLines(canvas);
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis(),
                difference = currentTime - lastUpdateTime;

        if (difference > 16) {
            if (horse != null) {
                horse.move();

                if (horse.getX() > w) {
                    horse.setX(DEFAULT_X);
                }

                setTextBubble();
                title.setTextPart(0, getDateInsertion());

                if (horse.getCurrentState() != Entity.Horse.State.FIRE_RUN
                        && Build.VERSION.SDK_INT >= 26) {
                    LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
                    if (!now.isBefore(LocalDateTime.of(2026, 1, 1, 0, 0))) {
                        horse.setCurrentState(Entity.Horse.State.FIRE_RUN);
                    }
                }
            }

            lastUpdateTime = currentTime;
        }
    }

    private void setTextBubble() {
        float x = horse.getX(), width = horse.getFrameWidth(),
                y = horse.getY(), height = horse.getFrameHeight() / 1.5f;
        textBubble.set(x + width, y - height * 2, x + width * 2.5f, y);
    }

    public void startAnimation() {
        isAnimating = true;
        lastUpdateTime = System.currentTimeMillis();
        invalidate();
    }

    public void stopAnimation() {
        isAnimating = false;
    }

    public void setHorseSpeed(float speed) {
        horseSpeed = speed;
        if (horse != null) {
            horse.setSpeedX(horseSpeed);
        }
    }

    public void setDisplayText(String text) {
        displayText = text;
        invalidate();
    }

    public String getDisplayText() {
        return displayText;
    }

    public float getHorseSpeed() {
        return horseSpeed;
    }

    public void setHorseState(Entity.Horse.State state) {
        horse.setCurrentState(state);
    }

    public void setSnowdriftCount(int count) {
        snowdriftCount = count;
        if (w > 0 && h > 0) {
            createSnowdrifts();
            invalidate();
        }
    }
}