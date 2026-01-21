package com.example.happynewyear2026;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import java.util.HashMap;
import java.util.TreeMap;

public class FlameDigitView extends FrameLayout {

    private boolean[][] digitPatterns;
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
        digitPatterns = new boolean[10][7];

        // 0: 012456
        digitPatterns[0] = new boolean[]{true, true, true, false, true, true, true};
        // 1: 25
        digitPatterns[1] = new boolean[]{false, false, true, false, false, true, false};
        // 2: 02346
        digitPatterns[2] = new boolean[]{true, false, true, true, true, false, true};
        // 3: 02356
        digitPatterns[3] = new boolean[]{true, false, true, true, false, true, true};
        // 4: 1235
        digitPatterns[4] = new boolean[]{false, true, true, true, false, true, false};
        // 5: 01356
        digitPatterns[5] = new boolean[]{true, true, false, true, false, true, true};
        // 6: 013456
        digitPatterns[6] = new boolean[]{true, true, false, true, true, true, true};
        // 7: 025
        digitPatterns[7] = new boolean[]{true, false, true, false, false, true, false};
        // 8: 0123456
        digitPatterns[8] = new boolean[]{true, true, true, true, true, true, true};
        // 9: 012356
        digitPatterns[9] = new boolean[]{true, true, true, true, false, true, true};

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
        if (digit < 0 || digit > 9 || currentDigit == digit) return;

        currentDigit = digit;
        boolean[] pattern = digitPatterns[digit];

        for (int i = 0; i < 7; i++) {
            segments[i].setVisibility(pattern[i] ? VISIBLE : INVISIBLE);
        }
    }

    public int getDigit() {
        return currentDigit;
    }

    public void setSecondaryRectSpeed(float speed) {
        for (int i = 0; i < segments.length; i++) {
            segments[i].setSecondaryRectSpeed(speed);
        }
    }

    public void setSecondaryRectSideFactor(float factor) {
        for (int i = 0; i < segments.length; i++) {
            segments[i].setSecondaryRectSideFactor(factor);
        }
    }
}