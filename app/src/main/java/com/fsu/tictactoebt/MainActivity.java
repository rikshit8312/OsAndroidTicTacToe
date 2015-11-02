package com.fsu.tictactoebt;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar action = getSupportActionBar();
        action.hide();
        GameSelectFragment gameSelectFragment = new GameSelectFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                gameSelectFragment).commit();

    }

}
