package com.fsu.tictacnolebt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Daniel Carroll on 7/5/2015.
 * Source code from: http://www.myandroidsolutions.com/2012/06/18/android-simple-splash-screen/
 */
public class SplashScreen extends Activity{

    private boolean mIsBackButtonPressed;
    private static final int SPLASH_DURATION = 6000; // 6 seconds


    /*
    On Create method to handle view
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            /*
            Run method for splash view
             */
            @Override
            public void run() {

                finish();

                //starts if back button not pressed, new intent here
                if (!mIsBackButtonPressed) {
                    Intent intent = new Intent(SplashScreen.this, BluetoothConnect.class);
                    SplashScreen.this.startActivity(intent);
                }

            }

        }, SPLASH_DURATION);

    }

    @Override
    public void onBackPressed() {

        // set the flag to true so the next activity won't start up
        mIsBackButtonPressed = true;
        super.onBackPressed();

    }

}
