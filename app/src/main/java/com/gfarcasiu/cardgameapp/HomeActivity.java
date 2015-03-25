package com.gfarcasiu.cardgameapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        setContentView(R.layout.activity_home);
    }

    public void hostGame(View view) {
        startActivity(new Intent(this, HostGameActivity.class));
    }

    public void joinGame(View view) {
        startActivity(new Intent(this, ClientActivity.class));
    }
}
