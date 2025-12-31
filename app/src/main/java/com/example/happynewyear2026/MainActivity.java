package com.example.happynewyear2026;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int GAME_ACTIVITY = 0;
    private static final int CONGRATS_ACTIVITY = 1;

    private FlameDigitView first, second, third, fourth;

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

        first.setDigit(2);
        second.setDigit(0);
        third.setDigit(2);
        fourth.setDigit(6);
    }

    public void play(View view) {
        Toast.makeText(this, "Игра пока не готова =(", Toast.LENGTH_LONG).show();

//        Intent gameActivity = new Intent();
//        startActivityForResult(gameActivity, GAME_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GAME_ACTIVITY:
                break;
            case CONGRATS_ACTIVITY:
                break;
        }
    }

    public void openCongrats(View view) {
        Intent congratsActivity = new Intent(MainActivity.this, CongratsActivity.class);
        startActivityForResult(congratsActivity, CONGRATS_ACTIVITY);
    }
}