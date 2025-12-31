package com.example.happynewyear2026;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import java.util.HashMap;

public class FlameDigitView extends FrameLayout {

    private HashMap<Integer, boolean[]> digitPatterns;
    private FlameRectangleView[] segments;
    private int currentDigit = -1;

    public FlameDigitView(Context context) {
        super(context);
        init(context);
    }

    public FlameDigitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlameDigitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Инициализация паттернов цифр
        digitPatterns = new HashMap<>();

        // 0: 0,1,2,4,5,6
        digitPatterns.put(0, new boolean[]{true, true, true, false, true, true, true});
        // 1: 2,5
        digitPatterns.put(1, new boolean[]{false, false, true, false, false, true, false});
        // 2: 0,2,3,4,6
        digitPatterns.put(2, new boolean[]{true, false, true, true, true, false, true});
        // 3: 0,2,3,5,6
        digitPatterns.put(3, new boolean[]{true, false, true, true, false, true, true});
        // 4: 1,2,3,5
        digitPatterns.put(4, new boolean[]{false, true, true, true, false, true, false});
        // 5: 0,1,3,5,6
        digitPatterns.put(5, new boolean[]{true, true, false, true, false, true, true});
        // 6: 0,1,3,4,5,6
        digitPatterns.put(6, new boolean[]{true, true, false, true, true, true, true});
        // 7: 0,2,5
        digitPatterns.put(7, new boolean[]{true, false, true, false, false, true, false});
        // 8: все сегменты
        digitPatterns.put(8, new boolean[]{true, true, true, true, true, true, true});
        // 9: 0,1,2,3,5,6
        digitPatterns.put(9, new boolean[]{true, true, true, true, false, true, true});

        segments = new FlameRectangleView[7];
        for (int i = 0; i < 7; i++) {
            segments[i] = new FlameRectangleView(context);
            addView(segments[i]);
            segments[i].setSecondaryRectSideFactor(0.1f);
            segments[i].setSecondaryRectSpeed(5f);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int width = right - left;
        int height = bottom - top;

        float segmentThickness = Math.min(width, height) * 0.2f;

        float segmentWidth = width - segmentThickness * 2;
        float segmentHeight = (height - 3 * segmentThickness) / 2;

        segments[0].layout(
                (int) segmentThickness,
                0,
                (int) (segmentThickness + segmentWidth),
                (int) segmentThickness
        );

//        segments[0].layout(
//                (int) 0,
//                0,
//                (int) (segmentWidth),
//                (int) segmentThickness
//        );

        segments[1].layout(
                0,
                (int) segmentThickness,
                (int) segmentThickness,
                (int) (segmentThickness + segmentHeight)
        );

//        segments[1].layout(
//                0,
//                (int) 0,
//                (int) segmentThickness,
//                (int) (segmentHeight)
//        );

        segments[2].layout(
                (int) (width - segmentThickness),
                (int) segmentThickness,
                width,
                (int) (segmentThickness + segmentHeight)
        );

//        segments[2].layout(
//                (int) (width - segmentThickness),
//                (int) 0,
//                width,
//                (int) (segmentHeight)
//        );

        segments[3].layout(
                (int) segmentThickness,
                (int) (segmentThickness + segmentHeight),
                (int) (segmentThickness + segmentWidth),
                (int) (segmentThickness + segmentHeight + segmentThickness)
        );

//        segments[3].layout(
//                (int) 0,
//                (int) (segmentHeight),
//                (int) (segmentWidth),
//                (int) (segmentThickness + segmentHeight)
//        );

        segments[4].layout(
                0,
                (int) (segmentThickness + segmentHeight + segmentThickness),
                (int) segmentThickness,
                (int) (segmentThickness + segmentHeight + segmentThickness + segmentHeight)
        );

//        segments[4].layout(
//                0,
//                (int) (segmentHeight),
//                (int) segmentThickness,
//                (int) (height)
//        );

        segments[5].layout(
                (int) (width - segmentThickness),
                (int) (segmentThickness + segmentHeight + segmentThickness),
                width,
                (int) (segmentThickness + segmentHeight + segmentThickness + segmentHeight)
        );

//        segments[5].layout(
//                (int) (width - segmentThickness),
//                (int) (segmentHeight),
//                width,
//                (int) (height)
//        );

        segments[6].layout(
                (int) segmentThickness,
                (int) (height - segmentThickness),
                (int) (segmentThickness + segmentWidth),
                height
        );

//        segments[6].layout(
//                (int) 0,
//                (int) (height - segmentThickness),
//                (int) (width),
//                height
//        );
    }

    public void setDigit(int digit) {
        if (digit < 0 || digit > 9) {
            throw new IllegalArgumentException("Digit must be between 0 and 9");
        }

        if (currentDigit == digit) return;

        currentDigit = digit;
        boolean[] pattern = digitPatterns.get(digit);

        for (int i = 0; i < 7; i++) {
            segments[i].setVisibility(pattern[i] ? VISIBLE : INVISIBLE);
        }
    }

    public int getDigit() {
        return currentDigit;
    }

    public void setSecondaryRectSpeed(float speed) {
        for (FlameRectangleView segment : segments) {
            segment.setSecondaryRectSpeed(speed);
        }
    }

    public void setSecondaryRectSideFactor(float factor) {
        for (FlameRectangleView segment : segments) {
            segment.setSecondaryRectSideFactor(factor);
        }
    }
}