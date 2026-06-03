package com.example.happynewyear2026;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_HIGH_SCORE = "highScore";
    public static final String KEY_CONGRATS_UNLOCKED = "congratsUnlocked";

    private static final int GAME_ACTIVITY = 0;
    private static final int CONGRATS_ACTIVITY = 1;

    private FlameDigitView first, second, third, fourth;
    private TextView play, tvOpenCongrats, newHighScore, tvCongratsLocked;
    private FlameRectangleView frvOpenCongrats;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private int highScore;
    private boolean congratsUnlocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        first = findViewById(R.id.first_FDV);
        second = findViewById(R.id.second_FDV);
        third = findViewById(R.id.third_FDV);
        fourth = findViewById(R.id.fourth_FDV);
        play = findViewById(R.id.play_TV);
        tvOpenCongrats = findViewById(R.id.open_congrats_TV);
        newHighScore = findViewById(R.id.new_high_score_TV);
        tvCongratsLocked = findViewById(R.id.congrats_locked_TV);
        frvOpenCongrats = findViewById(R.id.open_congrats_FRV);

        preferences = getPreferences(MODE_PRIVATE);
        highScore = preferences.getInt(KEY_HIGH_SCORE, 0);
        congratsUnlocked = preferences.getBoolean(KEY_CONGRATS_UNLOCKED, false);
        editor = preferences.edit();

        first.setDigit(2);
        second.setDigit(0);
        third.setDigit(2);
        fourth.setDigit(6);

        if (!congratsUnlocked) {
            int primary = ContextCompat.getColor(this, R.color.primary),
                    secondary = ContextCompat.getColor(this, R.color.secondary);
            int[] primaryARGB = {Color.alpha(primary) - 64,
                    Color.red(primary),
                    Color.green(primary),
                    Color.blue(primary)
            };
            int[] secondaryARGB = {Color.alpha(secondary) - 64,
                    Color.red(secondary),
                    Color.green(secondary),
                    Color.blue(secondary)
            };
            frvOpenCongrats.setColors(Color.argb(primaryARGB[0], primaryARGB[1], primaryARGB[2], primaryARGB[3]),
                    Color.argb(secondaryARGB[0], secondaryARGB[1], secondaryARGB[2], secondaryARGB[3]));
            frvOpenCongrats.setSecondaryRectSpeed(0);
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameActivity = new Intent(MainActivity.this, GameActivity.class);
                gameActivity.putExtra(KEY_HIGH_SCORE, highScore);
                startActivityForResult(gameActivity, GAME_ACTIVITY);
            }
        });
        tvOpenCongrats.setOnClickListener(new View.OnClickListener() {
            int clickCount;
            boolean timerStarted;

            @Override
            public void onClick(View view) {
                if (congratsUnlocked) {
                    Intent congratsActivity = new Intent(MainActivity.this, CongratsActivity.class);
                    startActivity(congratsActivity);
                } else if (!timerStarted) {
                    String out = "";
                    switch (clickCount) {
                        case 0:
                            out = "ПОЗДРАВЛЕНИЯ ЗАБЛОКИРОВАНЫ!\nНАБЕРИТЕ ОЧКОВ: " + GameActivity.MAX_CANDIES_COLLECTED + ", ЧТОБЫ ОТКРЫТЬ";
                            break;
                        case 1:
                            tvCongratsLocked.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.golden_yellow));
                            out = "ПОИГРАЙТЕ, ПОЖАЛУЙСТА,\n Я СТАРАЛСЯ ^_^";
                            break;
                        case 2:
                            out = "НУ ПОЖА-А-А-АЛУЙСТА =')";
                            break;
                        case 3:
                            congratsUnlocked = true;
                            editor.putBoolean(KEY_CONGRATS_UNLOCKED, true);
                            editor.commit();
                            frvOpenCongrats.reset();
                            out = "НУ ЛАДНО, ТАК УЖ И БЫТЬ...";
                            break;
                    }
                    clickCount++;
                    tvCongratsLocked.setText(out);
                    timerStarted = true;
                    new CountDownTimer(4000, 4000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            tvCongratsLocked.setText("");
                            timerStarted = false;
                        }
                    }.start();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case GAME_ACTIVITY:
                    int updHighScore = data.getIntExtra(KEY_HIGH_SCORE, 0);
                    if (updHighScore > highScore) {
                        highScore = updHighScore;
                        congratsUnlocked = true;
                        editor.putInt(KEY_HIGH_SCORE, highScore);
                        editor.putBoolean(KEY_CONGRATS_UNLOCKED, true);
                        editor.apply();
                        new CountDownTimer(3000, 400) {
                            int count;

                            @Override
                            public void onTick(long millisUntilFinished) {
                                String out = "НОВЫЙ РЕКОРД: " + highScore + "!";
                                if (count % 2 == 0) newHighScore.setText(out);
                                else newHighScore.setText("");
                                count++;
                            }

                            @Override
                            public void onFinish() {
                                newHighScore.setText("");
                            }
                        }.start();
                    }
                    break;
                case CONGRATS_ACTIVITY:
                    break;
            }
        }
    }
}