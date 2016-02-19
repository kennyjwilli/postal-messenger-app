package com.postalmessanger.messenger.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.postalmessanger.messenger.util.SLAPI;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Class<? extends Activity> activityClass;
        if (SLAPI.hasToken(this)) {
            activityClass = MainActivity.class;
        } else {
            activityClass = LoginActivity.class;
        }
        this.startActivity(new Intent(this, activityClass));
        finish();
    }


}
