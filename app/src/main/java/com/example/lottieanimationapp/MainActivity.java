// MainActivity.java
package com.example.lottieanimationapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private LottieAnimationView animationView;
    private Button playButton, pauseButton, restartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the animation view
        animationView = findViewById(R.id.lottie_animation);

        // Initialize buttons
        playButton = findViewById(R.id.btn_play);
        pauseButton = findViewById(R.id.btn_pause);
        restartButton = findViewById(R.id.btn_restart);

        // Set button click listeners
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView.playAnimation();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView.pauseAnimation();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView.cancelAnimation();
                animationView.setProgress(0);
                animationView.playAnimation();
            }
        });
    }
}