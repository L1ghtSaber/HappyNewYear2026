package com.example.happynewyear2026;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements GameEndListener {

    public static final int MAX_CANDIES_COLLECTED = 26, MAX_HORSE_LIFE_COUNT = 3;

    private static int COLOR_SKY;
    private static int COLOR_SNOW;
    private static int COLOR_BLACK = Color.BLACK;
    private static int COLOR_RED;
    private static int COLOR_GOLDEN;

    private Game game;
    private Game.Field field;
    private int highScore;
    private boolean newHighScore;
    private static final String SAVE_FILE_NAME = "game_save.json";
    private boolean properExit = false;
    private Gson gson = new Gson();

    private static Rect statusBar = new Rect(), navigationBar = new Rect();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        int statusBarHeight = 0;
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) statusBarHeight = getResources().getDimensionPixelSize(id);

        statusBar.set(0, 0, screenWidth, statusBarHeight);

        int navBarHeightPortrait = 0, navBarHeightLandscape = 0, navBarWidth = 0;
        id = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0) navBarHeightPortrait = getResources().getDimensionPixelSize(id);
        id = getResources().getIdentifier("navigation_bar_height_landscape", "dimen", "android");
        if (id > 0) navBarHeightLandscape = getResources().getDimensionPixelSize(id);
        id = getResources().getIdentifier("navigation_bar_width", "dimen", "android");
        if (id > 0) navBarWidth = getResources().getDimensionPixelSize(id);

        if (screenWidth > screenHeight) {
            if (navBarWidth > 0) {
                navigationBar.set(screenWidth - navBarWidth, 0, screenWidth, screenHeight);
            } else if (navBarHeightLandscape > 0) {
                navigationBar.set(0, screenHeight - navBarHeightLandscape, screenWidth, screenHeight);
            } else if (navBarHeightPortrait > 0) {
                navigationBar.set(0, screenHeight - navBarHeightPortrait, screenWidth, screenHeight);
            }
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setBarsColor();

        super.onCreate(savedInstanceState);

        COLOR_SKY = ContextCompat.getColor(this, R.color.deep_night);
        COLOR_SNOW = ContextCompat.getColor(this, R.color.dirty_white);
        COLOR_RED = ContextCompat.getColor(this, R.color.blood_red);
        COLOR_GOLDEN = ContextCompat.getColor(this, R.color.golden_yellow);

        highScore = getIntent().getIntExtra("highScore", 0);

        game = new Game();
        field = game.new Field(this);
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
    protected void onStop() {
        super.onStop();
        onGameEnd();
    }

    @Override
    public void onGameEnd() {
        if (field != null) {
            properExit = true;
            deleteSaveFile();
            field.endGame();
            Intent resultIntent = new Intent();
            resultIntent.putExtra(MainActivity.KEY_HIGH_SCORE, highScore);
            setResult(RESULT_OK, resultIntent);
        } else setResult(RESULT_CANCELED);
        restoreBarsColor();
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

    private void deleteSaveFile() {
        //new File(getFilesDir(), SAVE_FILE_NAME).delete();
    }

    enum GameMode {
        START, ACTIVE, PAUSE, CONFIRM_EXIT, END
    }

    private class Game {
        static final int SNOWDRIFT_COUNT = 10;
        final Random random = new Random();

        int w, h;
        float groundLevel;

        Entity.Horse horse;
        ArrayList<Entity.Snowdrift> snowdrifts = new ArrayList<>();
        ArrayList<Entity.Obstacle> obstacles = new ArrayList<>();
        ArrayList<Entity.Candy> candies = new ArrayList<>();
        ArrayList<Entity.Obstacle> snowballs = new ArrayList<>();
        Entity.Snowman snowman = null;

        int candiesCollected, horseLifeCount = MAX_HORSE_LIFE_COUNT;

//        public void resetGame() {
//            snowdrifts.clear();
//            obstacles.clear();
//            candies.clear();
//            snowballs.clear();
//            snowman = null;
//            horse = null;
//            candiesCollected = 0;
//            horseLifeCount = ;
//        }

        @SuppressLint("ViewConstructor")
        private class Field extends SurfaceView implements SurfaceHolder.Callback {
            private static final float DEFAULT_COORDINATE = -10f;

            private final Paint groundPaint;

            private ScreenDrawing screenDrawing;
            private GameUpdating gameUpdating;
            private Bitmap bufferBitmap;
            private Canvas bufferCanvas;

            private ComplexElement ceCandiesCollected, ceHorseLifeCount, ceGameRules, ceGameOver, ceBack, ceConfirm;
            private TextElement teUnlocked, teGameOnPause;
            private PathElement pePause;

            private RectF groundRect;

            private volatile boolean isRunning, isUpdated;
            private volatile GameMode mode;
            private float touchX = DEFAULT_COORDINATE, touchY = DEFAULT_COORDINATE;

            private float nextObstacleDistance;
            private long nextCandySpawnTime;
            private float lastSnowballX = -1;

            public Field(Context context) {
                super(context);
                getHolder().addCallback(this);
                //this.gameEndListener = gameEndListener;

                groundPaint = new Paint();
                groundPaint.setColor(COLOR_SNOW);
                groundPaint.setStyle(Paint.Style.FILL);
            }

            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                startGame();
            }

            public void startGame() {
                horse = new Entity.Horse(w * 0.1f, groundLevel, getContext());
                horse.setPaddingCollide(new float[]{0.3f, 0.1f, 0.1f, 0.2f});

                int horseWidth = horse.getFrameWidth();
                int horseHeight = horse.getFrameHeight();

                snowdrifts = Entity.Snowdrift.generateSnowdrifts(getContext(), SNOWDRIFT_COUNT, w, groundLevel,
                        horseWidth / 2, horseWidth,
                        0, horseHeight / 8);

                obstacles.clear();
                Entity.Obstacle firstObstacle = new Entity.Obstacle(w + horseWidth, groundLevel, Entity.Obstacle.Type.GIFT, getContext());
                obstacles.add(firstObstacle);

                candiesCollected = 0;
                horseLifeCount = MAX_HORSE_LIFE_COUNT;

                getNextObstacleDistance();
                getNextCandySpawnTime(System.currentTimeMillis());

                createInterface();

                setGroundRect();

                mode = GameMode.START;

                isRunning = true;
                isUpdated = false;

                gameUpdating = new GameUpdating();
                gameUpdating.start();

                screenDrawing = new ScreenDrawing();
                screenDrawing.start();

                bufferBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bufferCanvas = new Canvas(bufferBitmap);
            }

            private void createInterface() {
                int statusBarH = Math.min(statusBar.width(), statusBar.height()), size = 100,
                        navBarW = Math.min(navigationBar.width(), navigationBar.height());
                float indent = statusBarH / 4f;

                RectF candiesCollectedRect = new RectF(indent, statusBarH, indent + size, statusBarH + size);
                ceCandiesCollected = new ComplexElement(
                        new InterfaceElement[]{
                                new ImageElement(
                                        candiesCollectedRect,
                                        new Bitmap[]{Entity.decodeBitmap(getContext(), R.drawable.candy1)}
                                ),
                                new TextElement(
                                        new TextTitle(getContext(),
                                                new String[]{": ", "" + candiesCollected, " / ", "" + highScore},
                                                new RectF(candiesCollectedRect.right * 1.1f, candiesCollectedRect.top,
                                                        candiesCollectedRect.right + size, candiesCollectedRect.bottom),
                                                COLOR_SNOW, 60, TextTitle.Alignment.LEFT), false
                                )
                        },
                        1, 0, false
                );

                RectF horseLifeCountRect = new RectF(candiesCollectedRect.left, candiesCollectedRect.bottom * 1.1f,
                        candiesCollectedRect.right, candiesCollectedRect.bottom * 1.1f + size);
                ceHorseLifeCount = new ComplexElement(
                        new InterfaceElement[]{
                                new ImageElement(
                                        horseLifeCountRect,
                                        new Bitmap[]{Entity.decodeBitmap(getContext(), R.drawable.heart)}
                                ),
                                new TextElement(
                                        new TextTitle(getContext(),
                                                new String[]{": ", "" + horseLifeCount},
                                                new RectF(horseLifeCountRect.right * 1.1f, horseLifeCountRect.top,
                                                        horseLifeCountRect.right + size, horseLifeCountRect.bottom),
                                                COLOR_SNOW, 60, TextTitle.Alignment.LEFT), false
                                )
                        },
                        1, 0, false
                );

                RectF header = new RectF(0, statusBarH, w, statusBarH + 150);
                ceGameRules = new ComplexElement(
                        new InterfaceElement[]{
                                new TextElement(new TextTitle(getContext(), new String[]{"ПРАВИЛА"}, header, COLOR_SNOW, 90), false),
                                new TextElement(new TextTitle(getContext(),
                                        new String[]{
                                                "1. Нажимайте на экран, чтобы лошадь прыгала через препятствия\n",
                                                "2. Долгое нажатие или нажатие во время прыжка - более высокий прыжок\n",
                                                "3. Собирайте конфеты, каждая конфета даёт "
                                                        + Entity.Horse.getFireModeDuration() / 1000
                                                        + " сек. неуязвимости от препятствий\n",
                                                "4. Соберите конфет: " + MAX_CANDIES_COLLECTED + ", чтобы разблокировать поздравления\n",
                                                "5. Количество жизней: " + MAX_HORSE_LIFE_COUNT + ". При исчерпании жизней игра заканчивается\n",
                                                "6. Уворачивайтесь от снежков, которые кидает снеговик!\n",
                                                "7. Снеговика можно убить, прыгнув ему на голову\n",
                                                "8. Игру можно поставить на паузу - кнопкой в правом верхнем углу\n",
                                                "9. При внезапном выходе из игры, игровая сессия сохранятся,\n",
                                                "     а прогресс сбрасывается только после подтверждения выхода\n\n",
                                                "Удачи!\n\n\n",
                                                "Нажмите, чтобы начать игру"
                                        },
                                        new RectF(w * 0.1f, header.bottom, w, h), COLOR_SNOW, 40, TextTitle.Alignment.LEFT),
                                        true
                                )
                        },
                        0, 1, true
                );

                ceGameOver = new ComplexElement(
                        new InterfaceElement[]{
                                new TextElement(new TextTitle(getContext(), new String[]{"ИГРА ОКОНЧЕНА"},
                                        new RectF(0, 0, w, h), COLOR_RED, 150), false
                                ),
                                new TextElement(new TextTitle(getContext(), new String[]{"Нажмите на экран, чтобы продолжить"},
                                        new RectF(0, h * 0.8f, w, h), COLOR_RED, 40), false
                                )
                        },
                        0, 1, true
                );

                teGameOnPause = new TextElement(new TextTitle(getContext(), new String[]{"Игра на паузе"},
                        new RectF(0, 0, w, h), COLOR_SNOW, 100), false);

                teUnlocked = new TextElement(
                        new TextTitle(getContext(), new String[]{"ПОЗДРАВЛЕНИЯ РАЗБЛОКИРОВАНЫ!"}, header, COLOR_GOLDEN, 90),
                        false
                );

                RectF pauseRect = new RectF(w - navBarW - indent - size, statusBarH, w - navBarW - indent, statusBarH + size);
                float pL = pauseRect.left, pT = pauseRect.top, pR = pauseRect.right, pB = pauseRect.bottom,
                        pH = pauseRect.height(), pW = pauseRect.width();
                float startValue = 0.2f, stepW = 0.12f, stepH = 0.0625f;

                Path pausePath = new Path(), playPath = new Path();
                pausePath.addRect(pL + pW * startValue, pT + pH * startValue,
                        pR - pW * (1 - startValue * 2), pB - pH * startValue, Path.Direction.CW);
                pausePath.addRect(pL + pW * (1 - startValue * 2), pT + pH * startValue,
                        pR - pW * startValue, pB - pH * startValue, Path.Direction.CW);
                pausePath.close();

                for (int i = 0; i < 5; i++)
                    playPath.addRect(
                            pL + pW * (startValue + stepW * i),
                            pT + pH * (startValue + stepH * i),
                            pL + pW * (startValue + stepW * (i + 1)),
                            pB - pH * (startValue + stepH * i),
                            Path.Direction.CW);
                playPath.close();

                pePause = new PathElement(pauseRect, new Path[]{pausePath, playPath});
                pePause.setPaint(groundPaint);

                RectF backRect = new RectF(0, h - size, size, h);
                float bL = backRect.left, bT = backRect.top, bR = backRect.right, bB = backRect.bottom,
                        bW = backRect.width(), bH = backRect.height();

                Path backPath = new Path();
                backPath.addRect(bL + 0.2f * bW, bT + 0.45f * bH,
                        bR - 0.2f * bW, bB - 0.45f * bH, Path.Direction.CW);
                backPath.addRect(bL + 0.3f * bW, bT + 0.35f * bH,
                        bR - 0.6f * bW, bB - 0.35f * bH, Path.Direction.CW);
                backPath.addRect(bL + 0.4f * bW, bT + 0.25f * bH,
                        bR - 0.5f * bW, bB - 0.25f * bH, Path.Direction.CW);
                backPath.close();

                Paint backPaint = new Paint();
                backPaint.setStyle(Paint.Style.FILL);
                backPaint.setColor(COLOR_SNOW);
                PathElement peBack = new PathElement(backRect, new Path[]{backPath});
                peBack.setPaint(backPaint);

                ceBack = new ComplexElement(
                        new InterfaceElement[]{
                                peBack,
                                new TextElement(new TextTitle(getContext(), new String[]{"Выйти в меню"},
                                        new RectF(backRect.right * 1.1f, backRect.top,
                                                backRect.right + size * 3, backRect.bottom),
                                        backPaint.getColor(), 40, TextTitle.Alignment.LEFT), false
                                )
                        },
                        1, 0, false
                );

                TextTitle exitQuestion = new TextTitle(getContext(), new String[]{"Выйти из игры?"},
                        new RectF(0, h * 0.25f, w, h * 0.5f), COLOR_SNOW, 100);
                TextTitle areYouSure = new TextTitle(getContext(), new String[]{
                        "Весь прогресс будет утерян. Нажмите \"Да\", чтобы выйти." +
                                "\nНажмите на любую другую область экрана, чтобы вернуться"},
                        new RectF(0, 0, w, h * 0.1f), COLOR_SNOW, 40);
                TextTitle yes = new TextTitle(getContext(), new String[]{"Да"},
                        new RectF(0, 0, w, h * 0.2f), COLOR_SNOW, 100);
                yes.setInverse(true);

                ceConfirm = new ComplexElement(
                        new InterfaceElement[]{
                                new TextElement(exitQuestion, false),
                                new TextElement(areYouSure, true),
                                new TextElement(yes, false)
                        },
                        0, 1, false
                );
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
                onGameEnd();
            }

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                Game.this.w = w;
                Game.this.h = h;
                Game.this.groundLevel = h * 0.7f;
                setGroundRect();
            }

            private void resetTouch() {
                touchX = DEFAULT_COORDINATE;
                touchY = DEFAULT_COORDINATE;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                touchX = event.getX();
                touchY = event.getY();

                boolean actionDown = event.getAction() == MotionEvent.ACTION_DOWN;
                switch (mode) {
                    case ACTIVE:
                        if (actionDown) {
                            if (pePause.checkTouch(touchX, touchY)) {
                                mode = GameMode.PAUSE;
                                pePause.setCurrPath(1);
                                resetTouch();
                                break;
                            }
                        }
                        if (horse != null) horse.onTouchEvent(event);
                        break;
                    case START:
                        if (actionDown) {
                            mode = GameMode.ACTIVE;
                        }
                        break;
                    case PAUSE:
                        if (actionDown) {
                            if (pePause.checkTouch(touchX, touchY)) {
                                mode = GameMode.ACTIVE;
                                pePause.setCurrPath(0);
                                resetTouch();
                                break;
                            }
                            if (ceBack.checkTouch(touchX, touchY)) {
                                mode = GameMode.CONFIRM_EXIT;
                                resetTouch();
                                break;
                            }
                        }
                        break;
                    case CONFIRM_EXIT:
                        if (actionDown) {
                            if (ceConfirm.getElement(2).checkTouch(touchX, touchY)) {
                                onGameEnd();
                            } else mode = GameMode.PAUSE;
                        }
                        break;
                    case END:
                        if (actionDown) {
                            onGameEnd();
                        }
                        break;
                }

                return true;
            }

            private void getNextObstacleDistance() {
                nextObstacleDistance = horse.getFrameWidth() * (3 + random.nextInt(4));
            }

            private void getNextCandySpawnTime(long currentTime) {
                nextCandySpawnTime = currentTime + ((10 + random.nextInt(5)) * 1000);
            }

            private float getLastObstacleX() {
                float lastX = obstacles.isEmpty() ? w - 1 : obstacles.get(obstacles.size() - 1).getX();
                if (snowman != null && snowman.getX() > lastX) lastX = snowman.getX();
                return lastX;
            }

            private void spawnObstacle() {
                float lastX = getLastObstacleX();
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
                    float minX = getLastObstacleX() + test.getFrameWidth() * 2, maxX = getLastObstacleX() + nextObstacleDistance - test.getFrameWidth() * 2,
                            x = random.nextFloat() * (maxX - minX) + minX;

                    candies.add(new Entity.Candy(x, groundLevel, test.getType(), getContext()));

                    getNextCandySpawnTime(currentTime);
                }
            }

            private void spawnSnowball() {
                if (snowman != null) {
                    if (lastSnowballX - snowman.getX() >= horse.getFrameWidth() * 7) {
                        Entity.Obstacle snowball = snowman.spawnSnowball(getContext());
                        snowballs.add(snowball);

                        lastSnowballX = snowman.getX();
                    }
                }
            }

            private void spawnSnowDrift() {
                Entity.Snowdrift lastSnowdrift = snowdrifts.get(snowdrifts.size() - 1);

                if (lastSnowdrift.getBodyDraw().right < w) {
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

            private void updateInterface() {
                ((TextElement) ceCandiesCollected.getElement(1))
                        .setTextParts(new int[]{1, 3},
                                new String[]{"" + candiesCollected, "" + highScore});
                ((TextElement) ceHorseLifeCount.getElement(1))
                        .setTextParts(new int[]{1}, new String[]{"" + horseLifeCount});
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
                        candiesCollected++;
                        if (candiesCollected > highScore) {
                            highScore = candiesCollected;
                            newHighScore = true;
                        }
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

                if (horse.getTag() == Entity.Tag.NEED_TO_REMOVE) {
                    if (horseLifeCount > 1) {
                        horseLifeCount--;
                        horse.setTag(Entity.Tag.NONE, false);
                    } else {
                        horseLifeCount = 0;
                        mode = GameMode.END;
                    }
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

            private class GameUpdating extends Thread {
                private long targetFrameTime = 30;

                @Override
                public void run() {
                    while (isRunning) {
                        long startTime = System.currentTimeMillis();

                        if (mode != GameMode.END) {
                            updateInterface();
                            spawnSnowDrift();
                            spawnObstacle();
                            if (mode == GameMode.ACTIVE) {
                                spawnCandy(startTime);
                                spawnSnowball();
                                moveObjects();
                                removeObjects();
                                checkCollisions();
                            }
                        }

                        isUpdated = true;

                        long actualFrameTime = System.currentTimeMillis() - startTime;
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

            private class ScreenDrawing extends Thread {
                private final int LEVEL_DEFAULT = 0, LEVEL_OVERLAY = 1;

                //private boolean isDrawn;
                private int unlockedCount = -1;

                @Override
                public void run() {
                    while (isRunning) {
                        if (!isUpdated) continue;
                        isUpdated = false;

                        //if (isDrawn && mode != GameMode.ACTIVE) continue;
                        //isDrawn = false;

                        Canvas canvas = null;
                        try {
                            drawGameField(bufferCanvas);
                            drawInterface(bufferCanvas, LEVEL_DEFAULT);
                            drawObjects(bufferCanvas);
                            drawInterface(bufferCanvas, LEVEL_OVERLAY);

                            //if (mode != GameMode.ACTIVE) isDrawn = true;

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

                private void drawInterface(Canvas canvas, int level) {
                    if (level == LEVEL_DEFAULT) {
                        ceCandiesCollected.draw(canvas);
                        ceHorseLifeCount.draw(canvas);

                        if ((highScore < MAX_CANDIES_COLLECTED || highScore == MAX_CANDIES_COLLECTED && newHighScore)
                                && candiesCollected >= MAX_CANDIES_COLLECTED) {
                            if (unlockedCount < 0) unlockedCount = 0;
                            if (unlockedCount < 100) {
                                if (unlockedCount % 20 <= 10) teUnlocked.draw(canvas);
                                unlockedCount++;
                            }
                        }

                        if (mode != GameMode.PAUSE && mode != GameMode.CONFIRM_EXIT) {
                            pePause.draw(canvas);
                        }
                    } else if (level == LEVEL_OVERLAY) {
                        if (mode == GameMode.ACTIVE) return;

                        canvas.drawColor(Color.argb(128, 0, 0, 0));
                        switch (mode) {
                            case START:
                                ceGameRules.draw(canvas);
                                break;
                            case END:
                                ceGameOver.draw(canvas);
                                break;
                            case PAUSE:
                                teGameOnPause.draw(canvas);
                                pePause.draw(canvas);
                                ceBack.draw(canvas);
                                break;
                            case CONFIRM_EXIT:
                                ceConfirm.draw(canvas);
                                break;
                        }
                    }
                }

                private void drawObjects(Canvas canvas) {
                    boolean active = mode == GameMode.ACTIVE;

                    for (int i = 0; i < obstacles.size(); i++)
                        obstacles.get(i).draw(canvas, active);

                    if (snowman != null) snowman.draw(canvas, active);

                    for (int i = 0; i < candies.size(); i++) candies.get(i).draw(canvas, active);

                    for (int i = 0; i < snowballs.size(); i++)
                        snowballs.get(i).draw(canvas, active);

                    horse.draw(canvas, active);

                    for (int i = 0; i < snowdrifts.size(); i++)
                        snowdrifts.get(i).draw(canvas, active);
                }
            }

            private abstract class InterfaceElement {
                private Paint paint;
                private RectF body;

                public InterfaceElement(RectF body) {
                    this.body = body;

                    paint = new Paint();
                }

                public RectF getBody() {
                    return body;
                }

                public void setBody(RectF body) {
                    this.body = body;
                }

                public Paint getPaint() {
                    return paint;
                }

                public void setPaint(Paint paint) {
                    this.paint = paint;
                }

                public void setIndent(float x, float y) {
                    setBody(new RectF(x, y, x + body.width(), y + body.height()));
                }

                public boolean checkTouch(float x, float y) {
                    return x > body.left && x < body.right && y > body.top && y < body.bottom;
                }

                public abstract void draw(Canvas canvas);
            }

            private class ImageElement extends InterfaceElement {
                private Bitmap[] images;
                private int idx;

                public ImageElement(RectF body, Bitmap[] images) {
                    super(body);
                    this.images = images;
                }

                public void setCurrImage(int i) {
                    idx = i % images.length;
                }

                @Override
                public void draw(Canvas canvas) {
                    canvas.drawBitmap(images[idx], null, getBody(), getPaint());
                }
            }

            private class PathElement extends InterfaceElement {
                private Path[] paths;
                private int idx;

                public PathElement(RectF body, Path[] paths) {
                    super(body);
                    this.paths = paths;
                }

                public void setCurrPath(int i) {
                    idx = i % paths.length;
                }

                @Override
                public void draw(Canvas canvas) {
                    canvas.drawPath(paths[idx], getPaint());
                }
            }

            private class TextElement extends InterfaceElement {
                private TextTitle text;
                private boolean withLines;

                public TextElement(TextTitle text, boolean withLines) {
                    super(text.getBounds());
                    this.text = text;
                    this.withLines = withLines;
                }

                public void setWithLines(boolean withLines) {
                    this.withLines = withLines;
                }

                public void setTextParts(int[] indexes, String[] textParts) {
                    for (int i = 0; i < indexes.length; i++)
                        text.setTextPart(indexes[i], textParts[i]);
                }

                @Override
                public void setIndent(float x, float y) {
                    RectF b = getBody();
                    setBody(new RectF(x, y, x + b.width(), y + b.height()));
                    text.setBounds(b);
                }

                @Override
                public void setBody(RectF body) {
                    getBody().set(body);
                    text.setBounds(body);
                }

                @Override
                public boolean checkTouch(float x, float y) {
                    RectF b = text.getTextRect();
                    return x > b.left && x < b.right && y > b.top && y < b.bottom;
                }

                @Override
                public void draw(Canvas canvas) {
                    if (withLines) text.drawWithLines(canvas);
                    else text.drawWithoutLines(canvas);
                }
            }

            private class ComplexElement {
                private InterfaceElement[] elements;
                private int dirX, dirY;
                private boolean ignoreDir;

                public ComplexElement(InterfaceElement[] elements, int dirX, int dirY, boolean ignoreDir) {
                    this.elements = elements;
                    this.ignoreDir = ignoreDir;

                    setDirection(dirX, dirY);
                }

                public void draw(Canvas canvas) {
                    for (int i = 0; i < elements.length; i++) {
                        elements[i].draw(canvas);
                    }
                }

                public boolean checkTouch(float x, float y) {
                    for (int i = 0; i < elements.length; i++)
                        if (elements[i].checkTouch(x, y)) return true;
                    return false;
                }

                public InterfaceElement getElement(int i) {
                    return elements[i];
                }

                public void setDirection(int dirX, int dirY) {
                    this.dirX = dirX;
                    this.dirY = dirY;

                    if (ignoreDir) return;

                    for (int i = 1; i < elements.length; i++) {
                        float x, y;
                        if (elements[i] != null && elements[i - 1] != null) {
                            RectF prev = elements[i - 1].getBody(),
                                    curr = elements[i].getBody();
                            if (dirX == 0) x = prev.centerX() - curr.width() / 2;
                                //if (dirX == 0) x = prev.left;
                            else if (dirX > 0) x = prev.right;
                            else x = prev.left - curr.width();
                            if (dirY == 0) y = prev.centerY() - curr.height() / 2;
                                //if (dirY == 0) y = prev.top;
                            else if (dirY > 0) y = prev.bottom;
                            else y = prev.top - curr.height();

                            elements[i].setIndent(x, y);
                        }
                    }
                }
            }
        }
    }
}