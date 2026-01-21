package com.example.happynewyear2026;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements GameEndListener {

    private static int COLOR_SKY;
    private static int COLOR_SNOW;
    private static int COLOR_BLACK;

    private Game.Field field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setBarsColor();

        super.onCreate(savedInstanceState);

        COLOR_SKY = ContextCompat.getColor(this, R.color.deep_night);
        COLOR_SNOW = ContextCompat.getColor(this, R.color.dirty_white);
        COLOR_BLACK = ContextCompat.getColor(this, R.color.black);

        field = new Game.Field(this, this);
        setContentView(field);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onGameEnd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onGameEnd();
    }

    @Override
    public void onGameEnd() {
        restoreBarsColor();
        if (field != null) {
            field.endGame();
        }
        finish();
    }

    private void setBarsColor() {
        getWindow().setStatusBarColor(COLOR_BLACK);
        getWindow().setNavigationBarColor(COLOR_BLACK);
    }

    private void restoreBarsColor() {
        getWindow().setStatusBarColor(COLOR_SKY);
        getWindow().setNavigationBarColor(COLOR_SNOW);
    }

    static class Game {
        static final int SNOWDRIFT_COUNT = 10;
        static final Random random = new Random();

        static int w, h;
        static float groundLevel;

        static Entity.Horse horse;
        static ArrayList<Entity.Snowdrift> snowdrifts = new ArrayList<>();
        static ArrayList<Entity.Obstacle> obstacles = new ArrayList<>();
        static ArrayList<Entity.Candy> candies = new ArrayList<>();
        static ArrayList<Entity.Obstacle> snowballs = new ArrayList<>();
        static Entity.Snowman snowman = null;

        public static void resetGame() {
            snowdrifts.clear();
            obstacles.clear();
            candies.clear();
            snowballs.clear();
            snowman = null;
            horse = null;
        }

        @SuppressLint("ViewConstructor")
        static class Field extends SurfaceView implements SurfaceHolder.Callback {
            private final GameEndListener gameEndListener;
            private Paint groundPaint;

            private ScreenDrawing screenDrawing;
            private GameUpdating gameUpdating;
            private Bitmap bufferBitmap;
            private Canvas bufferCanvas;

            private RectF groundRect;

            volatile boolean isRunning, isUpdated;
            float touchX, touchY;

            private float nextObstacleDistance;
            private long nextCandySpawnTime;
            private float lastSnowballX = -1;

            public Field(Context context, GameEndListener gameEndListener) {
                super(context);
                getHolder().addCallback(this);
                this.gameEndListener = gameEndListener;

                groundPaint = new Paint();
                groundPaint.setColor(COLOR_SNOW);
                groundPaint.setStyle(Paint.Style.FILL);
            }

            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                startGame();
            }

            public void startGame() {
                resetGame();

                horse = new Entity.Horse(w * 0.1f, groundLevel, getContext());

                int horseWidth = horse.getFrameWidth();
                int horseHeight = horse.getFrameHeight();

                snowdrifts = Entity.Snowdrift.generateSnowdrifts(getContext(), SNOWDRIFT_COUNT, w, groundLevel,
                        horseWidth / 2, horseWidth,
                        0, horseHeight / 8
                );

                Entity.Obstacle firstObstacle = new Entity.Obstacle(w + horseWidth, groundLevel, Entity.Obstacle.Type.GIFT, getContext());
                obstacles.add(firstObstacle);

                getNextObstacleDistance();

                getNextCandySpawnTime(System.currentTimeMillis());

                setGroundRect();

                isRunning = true;
                isUpdated = false;

                gameUpdating = new GameUpdating();
                gameUpdating.start();

                screenDrawing = new ScreenDrawing();
                screenDrawing.start();

                bufferBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bufferCanvas = new Canvas(bufferBitmap);
            }

            public void endGame() {
                if (!isRunning) return;

                isRunning = false;
                if (gameUpdating != null) {
                    try {
                        gameUpdating.join();
                        gameUpdating = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (screenDrawing != null) {
                    try {
                        screenDrawing.join();
                        screenDrawing = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void setGroundRect() {
                if (groundRect == null) groundRect = new RectF();
                groundRect.set(0, groundLevel, w, h);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                endGame();
            }

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                Game.w = w;
                Game.h = h;
                Game.groundLevel = h * 0.6f;
                setGroundRect();
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                touchX = event.getX();
                touchY = event.getY();

                if (horse != null) {
                    horse.onTouchEvent(event);
                }

                return true;
            }

            private void getNextObstacleDistance() {
                nextObstacleDistance = horse.getFrameWidth() * (3 + random.nextInt(4));
            }

            private void getNextCandySpawnTime(long currentTime) {
                nextCandySpawnTime = currentTime + ((10 + random.nextInt(10)) * 1000);
            }

            private void spawnObstacle() {
                if (obstacles.isEmpty()) return;

                float lastX = obstacles.get(obstacles.size() - 1).getX();
                if (snowman != null && snowman.getX() > lastX) lastX = snowman.getX();
                if (lastX < w) {
                    float nextX = w + nextObstacleDistance;

                    if (random.nextFloat() < 0.2f && snowman == null) {
                        snowman = new Entity.Snowman(nextX, groundLevel, getContext());
                        lastSnowballX = nextX;
                    } else {
                        Entity.Obstacle.Type type;

                        if (random.nextFloat() < 0.5f) {
                            type = Entity.Obstacle.Type.GIFT;
                        } else {
                            type = Entity.Obstacle.Type.TALL_GIFT;
                        }

                        obstacles.add(new Entity.Obstacle(nextX, groundLevel, type, getContext()));
                    }

                    getNextObstacleDistance();
                }
            }

            private void spawnCandy(long currentTime) {
                if (currentTime > nextCandySpawnTime) {
                    Entity.Candy.Type[] types = Entity.Candy.Type.values();
                    Entity.Candy test = new Entity.Candy(0, 0, types[random.nextInt(types.length)], getContext());
                    float minX = w, maxX = w + nextObstacleDistance - test.getFrameWidth(),
                            x = random.nextFloat() * (maxX - minX) + minX;

                    candies.add(new Entity.Candy(x, groundLevel, test.getType(), getContext()));

                    getNextCandySpawnTime(currentTime);
                }
            }

            private void spawnSnowball() {
                if (snowman != null) {
                    if (lastSnowballX - snowman.getX() >= horse.getFrameWidth() * 5) {
                        Entity.Obstacle snowball = snowman.spawnSnowball(getContext());
                        snowballs.add(snowball);

                        lastSnowballX = snowman.getX();
                    }
                }
            }

            private void spawnSnowDrift() {
                Entity.Snowdrift lastSnowdrift = snowdrifts.get(snowdrifts.size() - 1);

                if (lastSnowdrift.getBody().right < w) {
                    Entity.Snowdrift newSnowdrift = Entity.Snowdrift.generateSnowdrift(
                            getContext(),
                            lastSnowdrift.getX() + lastSnowdrift.getFrameWidth(),
                            groundLevel,
                            horse.getFrameWidth() / 2,
                            horse.getFrameWidth(),
                            0,
                            horse.getFrameHeight() / 8
                    );
                    snowdrifts.add(newSnowdrift);
                }
            }

            private void moveObjects() {
                if (horse != null) horse.move();

                if (snowman != null) snowman.move();

                for (int i = 0; i < obstacles.size(); i++) obstacles.get(i).move();

                for (int i = 0; i < candies.size(); i++) candies.get(i).move();

                for (int i = 0; i < snowballs.size(); i++) snowballs.get(i).move();

                for (int i = 0; i < snowdrifts.size(); i++) snowdrifts.get(i).move();
            }

            private void removeObjects() {
                for (Iterator<Entity.Snowdrift> iterator = snowdrifts.iterator(); iterator.hasNext(); ) {
                    Entity.Snowdrift snowdrift = iterator.next();
                    if (snowdrift.getTag() == Entity.Tag.NEED_TO_REMOVE) {
                        iterator.remove();
                    }
                }

                for (Iterator<Entity.Obstacle> iterator = obstacles.iterator(); iterator.hasNext(); ) {
                    Entity.Obstacle obstacle = iterator.next();
                    if (obstacle.getTag() == Entity.Tag.NEED_TO_REMOVE) {
                        iterator.remove();
                    }
                }

                for (Iterator<Entity.Candy> iterator = candies.iterator(); iterator.hasNext(); ) {
                    Entity.Candy candy = iterator.next();
                    if (candy.getTag() == Entity.Tag.NEED_TO_REMOVE) {
                        iterator.remove();
                    }
                }

                for (Iterator<Entity.Obstacle> iterator = snowballs.iterator(); iterator.hasNext(); ) {
                    Entity.Obstacle snowball = iterator.next();
                    if (snowball.getTag() == Entity.Tag.NEED_TO_REMOVE) {
                        iterator.remove();
                    }
                }

                if (snowman != null && snowman.getTag() == Entity.Tag.NEED_TO_REMOVE) {
                    snowman = null;
                }
            }

            private void checkCollisions() {
                if (horse == null) return;

                for (Entity.Obstacle obstacle : obstacles) {
                    if (obstacle.collidesWithHorse(horse)) {

                    }
                }

                for (Entity.Candy candy : candies) {
                    if (candy.collidesWithHorse(horse)) {

                    }
                }

                if (snowman != null) {
                    if (snowman.collidesWithHorse(horse)) {

                    }
                }

                for (Entity.Obstacle snowball : snowballs) {
                    if (snowball.collidesWithHorse(horse)) {

                    }
                }
            }

            class GameUpdating extends Thread {
                long targetFrameTime = 30;

                @Override
                public void run() {
                    while (isRunning) {
                        long startTime = SystemClock.elapsedRealtime();

                        spawnCandy(startTime);
                        spawnSnowDrift();
                        spawnObstacle();
                        spawnSnowball();
                        moveObjects();
                        removeObjects();
                        checkCollisions();

                        isUpdated = true;

                        long actualFrameTime = SystemClock.elapsedRealtime() - startTime;
                        long sleepTime = targetFrameTime - actualFrameTime;
                        if (sleepTime > 0) {
                            try {
                                sleep(sleepTime);
                            } catch (InterruptedException e) {
                                currentThread().interrupt();
                            }
                        }
                    }
                }
            }

            class ScreenDrawing extends Thread {
                @Override
                public void run() {
                    while (isRunning) {
                        if (!isUpdated) continue;
                        isUpdated = false;

                        Canvas canvas = null;
                        try {
                            drawGameField(bufferCanvas);
                            drawObjects(bufferCanvas);

                            canvas = getHolder().lockCanvas();
                            if (canvas != null) {
                                canvas.drawBitmap(bufferBitmap, 0, 0, null);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (canvas != null) {
                                try {
                                    getHolder().unlockCanvasAndPost(canvas);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                private void drawGameField(Canvas canvas) {
                    canvas.drawColor(COLOR_SKY);
                    canvas.drawRect(groundRect, groundPaint);
                }

                private void drawObjects(Canvas canvas) {
                    for (int i = 0; i < obstacles.size(); i++) obstacles.get(i).draw(canvas);

                    if (snowman != null) snowman.draw(canvas);

                    for (int i = 0; i < candies.size(); i++) candies.get(i).draw(canvas);

                    for (int i = 0; i < snowballs.size(); i++) snowballs.get(i).draw(canvas);

                    horse.draw(canvas);

                    for (int i = 0; i < snowdrifts.size(); i++) snowdrifts.get(i).draw(canvas);
                }
            }
        }
    }
}