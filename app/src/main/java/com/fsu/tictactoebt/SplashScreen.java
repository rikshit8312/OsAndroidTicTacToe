package com.fsu.tictactoebt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;


public class SplashScreen extends Activity {


    private static final int SPLASH_DURATION = 10000;
    ProgressBar mProgressBar;
    boolean mbActive;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);


        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        Thread timerThread = new Thread() {
            @Override
            public void run() {
                mbActive = true;
                try {
                    int waited = 0;
                    while (mbActive && (waited < SPLASH_DURATION)) {
                        sleep(150);
                        if (mbActive) {
                            waited += 500;
                            updateProgress(waited);
                        }
                    }
                } catch (InterruptedException e) {
                } finally {
                    onContinue();
                }
            }
        };
        timerThread.start();
    }

    public void updateProgress(final int timePasses) {
        if (mProgressBar != null) {
            final int progress = mProgressBar.getMax() * timePasses / SPLASH_DURATION;
            mProgressBar.setProgress(progress);

        }
    }

    public void onContinue() {
        Intent intent = new Intent(SplashScreen.this, BluetoothConnect.class);
        startActivity(intent);
        finish();
    }
}



