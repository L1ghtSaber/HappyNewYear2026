package com.example.happynewyear2026;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;

public class Entity {
    public static final float GRAVITY = 0.6f;
    public static final float DEFAULT_SPEED_Y = -16f;
    public static final float DEFAULT_SPEED_X = -8f;
    public static final float FAST_SPEED_X = -16f;
    public static final int DEFAULT_TICK_TIME = 100;

    protected static final Random random = new Random();

    public enum Tag {
        NONE, NEED_TO_REMOVE, IRREMOVABLE
    }

    private float x, y;
    private float speedX, speedY;
    private RectF body;
    private Bitmap sprite;
    private Rect[][] frames;

    private int currentFrameRow, currentFrameCol;
    private int tickTime = DEFAULT_TICK_TIME;
    private Tag tag = Tag.NONE;
    private long lastFrameTime;
    private int frameWidth, frameHeight;
    private int frameRowCount, frameColCount;

    public Entity(float x, float y) {
        this.x = x;
        this.y = y;
        body = new RectF();
        updateBody();
        lastFrameTime = System.currentTimeMillis();
    }

    public boolean collidesWith(Entity other) {
        return RectF.intersects(this.getBody(), other.getBody());
    }

    public void draw(Canvas canvas) {
        if (sprite == null || frames == null || frameRowCount == 0 || frameColCount == 0) return;

        if (tag == Tag.NEED_TO_REMOVE) return;

        canvas.drawBitmap(sprite, frames[currentFrameRow][currentFrameCol], body, null);

        changeFrame();
    }

    public void move() {
        x += speedX;
        y += speedY;

        if (x + frameWidth < 0) setTag(Tag.NEED_TO_REMOVE, false);

        updateBody();
    }

    protected void changeFrame() {
        if (frames == null || frames.length == 0) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > tickTime) {
            currentFrameCol = (currentFrameCol + 1) % frameColCount;
            lastFrameTime = currentTime;
        }
    }

    protected void changeFrameRow(int frameRow) {
        currentFrameRow = frameRow % frameRowCount;
    }

    protected void updateBody() {
        body.set(x, y, x + frameWidth, y + frameHeight);
    }

    protected void decodeBitmapToSprite(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap sprite = BitmapFactory.decodeResource(
                context.getResources(),
                resId,
                options
        );
        setSprite(sprite);
    }

    public float getX() { return x; }
    public void setX(float x) {
        this.x = x;
        updateBody();
    }

    public float getY() { return y; }
    public void setY(float y) {
        this.y = y;
        updateBody();
    }

    public float getSpeedX() { return speedX; }
    public void setSpeedX(float speedX) { this.speedX = speedX; }

    public float getSpeedY() { return speedY; }
    public void setSpeedY(float speedY) { this.speedY = speedY; }

    public int getCurrentFrameRow() {
        return currentFrameRow;
    }

    public int getCurrentFrameCol() {
        return currentFrameCol;
    }

    public RectF getBody() { return body; }

    public Bitmap getSprite() { return sprite; }
    public void setSprite(Bitmap sprite) { this.sprite = sprite; }

    public Rect[][] getFrames() { return frames; }
    public void setFrames(int rowsCount, int colsCount) {
        frames = new Rect[rowsCount][colsCount];
        currentFrameCol = 0;
        currentFrameRow = 0;
        frameRowCount = rowsCount;
        frameColCount = colsCount;

        if (sprite == null) return;

        setFrameHeight(sprite.getHeight() / rowsCount);
        setFrameWidth(sprite.getWidth() / colsCount);

        for (int i = 0; i < rowsCount; i++) {
            for (int j = 0; j < colsCount; j++) {
                frames[i][j] = new Rect(j * frameWidth, i * frameHeight, (j + 1) * frameWidth, (i + 1) * frameHeight);
            }
        }
    }

    public int getTickTime() { return tickTime; }
    public void setTickTime(int tickTime) { this.tickTime = tickTime; }

    public int getFrameWidth() { return frameWidth; }
    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
        updateBody();
    }

    public int getFrameHeight() { return frameHeight; }
    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
        updateBody();
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag, boolean forced) {
        if (this.tag == Tag.IRREMOVABLE && !forced) return;

        this.tag = tag;
    }

    public long getLastFrameTime() {
        return lastFrameTime;
    }

    public void setLastFrameTime(long lastFrameTime) {
        this.lastFrameTime = lastFrameTime;
    }

    public int getFrameRowCount() {
        return frameRowCount;
    }

    public int getFrameColCount() {
        return frameColCount;
    }

    public static class Snowdrift extends Entity {

        private final Paint snowPaint;

        public Snowdrift(float x, float y, int width, int height, Context context) {
            super(x, y);

            setFrameWidth(width);
            setFrameHeight(height);
            setSpeedX(DEFAULT_SPEED_X);
            updateBody();

            snowPaint = new Paint();
            snowPaint.setStyle(Paint.Style.FILL);
            snowPaint.setColor(ContextCompat.getColor(context, R.color.dirty_white));
        }

        @Override
        public void draw(Canvas canvas) {
            if (getTag() == Tag.NEED_TO_REMOVE) return;

            canvas.drawRect(getBody(), snowPaint);
        }

        public static Snowdrift generateSnowdrift(Context context, float x, float groundLevel, int minWidth, int maxWidth, int minHeight, int maxHeight) {
            x += minWidth / (random.nextInt(2) + 1.f);
            int width = minWidth + random.nextInt(maxWidth - minWidth);
            int height = minHeight + random.nextInt(maxHeight - minHeight);
            float y = groundLevel - height;
            return new Entity.Snowdrift(x, y, width, height, context);
        }

        public static ArrayList<Snowdrift> generateSnowdrifts(Context context, int count, int fieldWidth, float groundLevel,
                                                   int minWidth, int maxWidth,
                                                   int minHeight, int maxHeight) {
            ArrayList<Snowdrift> snowdrifts = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                float x = ((float) fieldWidth / (count + 1)) * i;

                snowdrifts.add(generateSnowdrift(context, x, groundLevel, minWidth, maxWidth, minHeight, maxHeight));
            }

            return snowdrifts;
        }

    }

    public static class Horse extends Entity {

//        private static final int FRAME_WIDTH = 56*4;
//        private static final int FRAME_HEIGHT = 32*4;

        public enum State {
            NORMAL_RUN,
            NORMAL_JUMP,
            FIRE_RUN,
            FIRE_JUMP
        }

        private static final int MAX_JUMP_HOLD = 500;

        private State currentState;
        private boolean isJumping;
        private float groundY;
        private long jumpStartTime;
        private boolean jumpPressed;

        private boolean isUpdatable;

        private boolean isFireMode;
        private long fireModeStartTime;
        private long fireModeDuration = 3000;

        private final float minJumpHeight;

        public Horse(float x, float groundY, Context context) {
            super(x, 0);
            this.groundY = groundY;
            this.currentState = State.NORMAL_RUN;
            this.isJumping = false;
            this.isFireMode = false;
            this.isUpdatable = true;

            decodeBitmapToSprite(context, R.drawable.horse_sprite);

//            setFrameWidth(FRAME_WIDTH);
//            setFrameHeight(FRAME_HEIGHT);

            setFrames(State.values().length, 5);
            setY(groundY - getFrameHeight());

            minJumpHeight = getFrameHeight();
        }

        @Override
        public void move() {
            super.move();

            updateFireMode();

            if (isJumping) setSpeedY(getSpeedY() + GRAVITY);

            if (getY() > groundY - getFrameHeight()) {
                setY(groundY - getFrameHeight());
                setSpeedY(0);
                isJumping = false;

                updateState();
            }

            if (jumpPressed && !isJumping) startJump();

            if (isJumping && jumpPressed) applyAdditionalJumpHeight();
        }

        private void startJump() {
            isJumping = true;

            setSpeedY(DEFAULT_SPEED_Y);

            jumpStartTime = System.currentTimeMillis();
            updateState();
        }

        private void applyAdditionalJumpHeight() {
            long currentTime = System.currentTimeMillis();
            long holdTime = currentTime - jumpStartTime;

            if (holdTime < MAX_JUMP_HOLD)
                setSpeedY(-(float) Math.sqrt(2 * GRAVITY * minJumpHeight));
        }

        private void updateState() {
            if (!isUpdatable) return;

            State newState;

            if (isFireMode) {
                newState = isJumping ? State.FIRE_JUMP : State.FIRE_RUN;
            } else {
                newState = isJumping ? State.NORMAL_JUMP : State.NORMAL_RUN;
            }

            if (currentState != newState) {
                currentState = newState;
            }

            changeFrameRow(currentState.ordinal());
        }

        private void updateFireMode() {
            if (isFireMode) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - fireModeStartTime > fireModeDuration) {
                    isFireMode = false;
                    updateState();
                }
            }
        }

        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    jumpPressed = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    jumpPressed = false;
                    return true;
            }
            return false;
        }

        public void activateFireMode() {
            if (!isFireMode) {
                isFireMode = true;
                fireModeStartTime = System.currentTimeMillis();
                updateState();
            }
        }

        public boolean isInFireMode() {
            return isFireMode;
        }

        public boolean isJumping() {
            return isJumping;
        }

        public State getCurrentState() {
            return currentState;
        }

        public void setCurrentState(State currentState) {
            this.currentState = currentState;
            changeFrameRow(currentState.ordinal());
        }

        public boolean isUpdatable() {
            return isUpdatable;
        }

        public void setUpdatable(boolean updatable) {
            isUpdatable = updatable;
        }

        public void setGroundY(float groundY) {
            this.groundY = groundY;
            if (!isJumping) {
                setY(groundY - getFrameHeight());
            }
        }

        public void reset() {
            setY(groundY - getFrameHeight());
            setSpeedY(0);
            isJumping = false;
            isFireMode = false;
            jumpPressed = false;
            updateState();
        }
    }

    public static class Candy extends Entity implements Collidable {
        public enum Type {
            CANDY1, CANDY2, CANDY3
        }

        private Type type;
        private boolean collected;

        public Candy(float x, float groundY, Type type, Context context) {
            super(x, 0);
            this.type = type;
            this.collected = false;

            int resId;
            switch (type) {
                case CANDY2:
                    resId = R.drawable.candy2;
                    break;
                case CANDY3:
                    resId = R.drawable.candy3;
                    break;
                default:
                    resId = R.drawable.candy1;
                    break;
            }

            decodeBitmapToSprite(context, resId);

            setFrames(1, 1);

            setY(groundY - getFrameHeight());
        }

        public Type getType() {
            return type;
        }

        public boolean isCollected() {
            return collected;
        }

        @Override
        public boolean collidesWithHorse(Horse horse) {
            if (!collected && collidesWith(horse)) {
                collected = true;
                horse.activateFireMode();
                setTag(Tag.NEED_TO_REMOVE, false);
                return true;
            }
            return false;
        }
    }

    public static class Obstacle extends Entity implements Collidable {

        public enum Type {
            GIFT, TALL_GIFT, SNOWBALL
        }

        private Type type;

        public Obstacle(float x, float groundY, Type type, Context context) {
            super(x, 0);
            this.type = type;

            int resId;
            switch (type) {
                case TALL_GIFT:
                    resId = R.drawable.tall_gift;
                    setSpeedX(DEFAULT_SPEED_X);
                    break;
                case SNOWBALL:
                    resId = R.drawable.snowball;
                    setSpeedX(FAST_SPEED_X);
                    break;
                default:
                    resId = R.drawable.gift;
                    setSpeedX(DEFAULT_SPEED_X);
                    break;
            }

            decodeBitmapToSprite(context, resId);

            setFrames(1, 1);

            setY(groundY - getFrameHeight());
        }

        public Type getType() {
            return type;
        }

        @Override
        public boolean collidesWithHorse(Horse horse) {
            if (collidesWith(horse)) {
                if (horse.isInFireMode()) {
                    setTag(Tag.NEED_TO_REMOVE, false);
                }
                else {
//                    horse.setUpdatable(false);
//                    horse.setTag(Tag.NEED_TO_REMOVE, false);
                }
                return true;
            }

            return false;
        }
    }

    public static class Snowman extends Entity implements Collidable {

        public Snowman(float x, float groundY, Context context) {
            super(x, 0);

            setSpeedX(DEFAULT_SPEED_X);

            decodeBitmapToSprite(context, R.drawable.snowman);

            setFrames(1, 1);

            setY(groundY - getFrameHeight());
        }

        public Obstacle spawnSnowball(Context context) {
            return new Obstacle(getX(), getY() + getFrameHeight() * 0.5f, Obstacle.Type.SNOWBALL, context);
        }

        @Override
        public boolean collidesWithHorse(Horse horse) {
            if (collidesWith(horse)) {
                if (horse.isInFireMode()) {
                    setTag(Tag.NEED_TO_REMOVE, false);
                }
                else {
                    if (horse.isJumping()) {
                        if (horse.getBody().bottom < getBody().top + getBody().width() / 8) {
                            setTag(Tag.NEED_TO_REMOVE, false);
                        }
                        else {
//                          horse.setUpdatable(false);
//                          horse.setTag(Tag.NEED_TO_REMOVE, false);
                        }
                    }
                }
                return true;
            }

            return false;
        }
    }

    public interface Collidable {
        boolean collidesWithHorse(Horse horse);
    }
}
