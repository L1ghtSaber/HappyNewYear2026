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

public class Entity {
    public enum Tag {
        NONE, NEED_TO_REMOVE
    }

    private float x, y;
    private float speedX, speedY;
    private RectF body;
    private Bitmap sprite;
    private int[][] frames;
    private int currentFrame;
    private int tickTime = 100;
    private Tag tag = Tag.NONE;

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public long getLastFrameTime() {
        return lastFrameTime;
    }

    public void setLastFrameTime(long lastFrameTime) {
        this.lastFrameTime = lastFrameTime;
    }

    private long lastFrameTime;
    private int frameWidth, frameHeight;

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
        if (sprite == null || frames == null || frames.length == 0) return;

        if (tag == Tag.NEED_TO_REMOVE) return;

        int row = frames[currentFrame][0];
        int col = frames[currentFrame][1];

        canvas.drawBitmap(
                sprite,
                new Rect(col * frameWidth, row * frameHeight, col * frameWidth + frameWidth, row * frameHeight + frameHeight),
                body, null
        );
    }

    public void move() {
        x += speedX;
        y += speedY;
        updateBody();
        changeFrame();
    }

    protected void changeFrame() {
        if (frames == null || frames.length == 0) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > tickTime) {
            currentFrame = (currentFrame + 1) % frames.length;
            lastFrameTime = currentTime;
        }
    }

    protected void updateBody() {
        body.set(x, y, x + frameWidth, y + frameHeight);
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

    public RectF getBody() { return body; }

    public Bitmap getSprite() { return sprite; }
    public void setSprite(Bitmap sprite) { this.sprite = sprite; }

    public int[][] getFrames() { return frames; }
    public void setFrames(int[][] frames) {
        this.frames = frames;
        if (frames != null && frames.length > 0) {
            currentFrame = 0;
        }
    }

    public int getCurrentFrame() { return currentFrame; }
    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
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

    public static class Snowdrift extends Entity {

        private Paint snowPaint;

        public Snowdrift(float x, float y, int width, int height, Context context) {
            super(x, y);

            setFrameWidth(width);
            setFrameHeight(height);
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

    }

    public static class Horse extends Entity {

        private static final int FRAME_WIDTH = 56*4;
        private static final int FRAME_HEIGHT = 32*4;

        public enum State {
            NORMAL_RUN,
            NORMAL_JUMP,
            FIRE_RUN,
            FIRE_JUMP
        }

        private static final float GRAVITY = 0.5f;
        private static final float JUMP_FORCE = -15f;
        private static final float MAX_JUMP_HOLD = 500f;
        private static final float MAX_ADDITIONAL_FORCE = -5f;
        private static final int FRAMES_PER_ROW = 5;

        private State currentState;
        private boolean isJumping;
        private float groundY;
        private long jumpStartTime;
        private boolean jumpButtonPressed;

        private boolean isUpdatable;

        private boolean isFireMode;
        private long fireModeStartTime;
        private long fireModeDuration = 3000;

        public Horse(float x, float groundY, Context context) {
            super(x, groundY - FRAME_HEIGHT);
            this.groundY = groundY;
            this.currentState = State.NORMAL_RUN;
            this.isJumping = false;
            this.isFireMode = false;
            this.isUpdatable = true;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap sprite = BitmapFactory.decodeResource(
                    context.getResources(),
                    R.drawable.horse_sprite,
                    options
            );
            setSprite(sprite);

            setFrameWidth(FRAME_WIDTH);
            setFrameHeight(FRAME_HEIGHT);

            int length = FRAMES_PER_ROW * State.values().length;
            setFrames(new int[length][2]);
            for (int i = 0; i < length; i++) {
                getFrames()[i][0] = i / 5;
                getFrames()[i][1] = i % 5;
            }

            setTickTime(100);
        }

        @Override
        protected void changeFrame() {
            if (getFrames() == null || getFrames().length == 0) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime - getLastFrameTime() > getTickTime()) {
                setCurrentFrame((getCurrentFrame() + 1) % FRAMES_PER_ROW + FRAMES_PER_ROW * currentState.ordinal());
                setLastFrameTime(currentTime);
            }
        }

        @Override
        public void move() {
            super.move();

            updateFireMode();

            if (isJumping) {
                setSpeedY(getSpeedY() + GRAVITY);
            }

            if (getY() >= groundY - getFrameHeight()) {
                setY(groundY - getFrameHeight());
                setSpeedY(0);
                isJumping = false;

                if (!jumpButtonPressed) {
                    updateState();
                }
            }

            if (jumpButtonPressed && !isJumping) {
                startJump();
            }

            if (isJumping && jumpButtonPressed) {
                applyAdditionalJumpForce();
            }
        }

        private void startJump() {
            isJumping = true;

            setSpeedY(JUMP_FORCE);
            jumpStartTime = System.currentTimeMillis();

            updateState();
        }

        private void applyAdditionalJumpForce() {
            long currentTime = System.currentTimeMillis();
            long holdTime = currentTime - jumpStartTime;

            if (holdTime < MAX_JUMP_HOLD && getSpeedY() < 0) {
                float progress = (float) holdTime / MAX_JUMP_HOLD;
                float additionalForce = MAX_ADDITIONAL_FORCE * progress;
                setSpeedY(getSpeedY() + additionalForce);
            }
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
                    jumpButtonPressed = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    jumpButtonPressed = false;
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
            jumpButtonPressed = false;
            updateState();
        }
    }

//    public static class Candy extends Entity {
//        public enum Type {
//            CANDY1, CANDY2, CANDY3
//        }
//
//        private Type type;
//        private boolean collected;
//
//        public Candy(float x, float y, Type type, Context context) {
//            super(x, y);
//            this.type = type;
//            this.collected = false;
//
//            int resId = 0;
////            switch (type) {
////                case CANDY2:
////                    resId = R.drawable.candy2;
////                    break;
////                case CANDY3:
////                    resId = R.drawable.candy3;
////                    break;
////                default:
////                    resId = R.drawable.candy1;
////                    break;
////            }
//
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inScaled = false;
//            Bitmap sprite = BitmapFactory.decodeResource(
//                    context.getResources(),
//                    resId,
//                    options
//            );
//            setSprite(sprite);
//            setFrameWidth(sprite.getWidth());
//            setFrameHeight(sprite.getHeight());
//            setFrames(new int[][]{{0, 0}});
//        }
//
//        public Type getType() {
//            return type;
//        }
//
//        public boolean isCollected() {
//            return collected;
//        }
//
//        public void setCollected(boolean collected) {
//            this.collected = collected;
//        }
//
//        public boolean collideWithHorse(Horse horse) {
//            if (!collected && collidesWith(horse)) {
//                collected = true;
//                horse.activateFireMode();
//                setTag(Tag.NEED_TO_REMOVE);
//                return true;
//            }
//            return false;
//        }
//    }

//    public static class Obstacle extends Entity {
//        public enum Type {
//            GIFT, TALL_GIFT, SNOWBALL
//        }
//
//        private Type type;
//        private boolean passed;
//
//        public Obstacle(float x, float y, Type type, Context context) {
//            super(x, y);
//            this.type = type;
//            this.passed = false;
//
//            int resId = 0;
////            switch (type) {
////                case TALL_GIFT:
////                    resId = R.drawable.tall_gift;
////                    setSpeedX(-8.0f);
////                    break;
////                case SNOWBALL:
////                    resId = R.drawable.snowball;
////                    setSpeedX(-14.0f);
////                    break;
////                default:
////                    resId = R.drawable.gift;
////                    setSpeedX(-8.0f);
////                    break;
////            }
//
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inScaled = false;
//            Bitmap sprite = BitmapFactory.decodeResource(
//                    context.getResources(),
//                    resId,
//                    options
//            );
//            setSprite(sprite);
//            setFrameWidth(sprite.getWidth());
//            setFrameHeight(sprite.getHeight());
//            setFrames(new int[][]{{0, 0}});
//        }
//
//        public Type getType() {
//            return type;
//        }
//
//        public boolean isPassed() {
//            return passed;
//        }
//
//        public void setPassed(boolean passed) {
//            this.passed = passed;
//        }
//
//        public boolean collideWithHorse(Horse horse) {
//            if (getBody().intersect(horse.getBody())) {
//                if (!horse.isInFireMode()) {
//                    horse.setUpdatable(false);
//                }
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public void move() {
//            super.move();
//            if (getX() + getFrameWidth() < 0) {
//            }
//        }
//    }
}
