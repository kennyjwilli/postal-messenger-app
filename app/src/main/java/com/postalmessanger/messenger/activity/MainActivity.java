package com.postalmessanger.messenger.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.postalmessanger.messenger.R;
import com.postalmessanger.messenger.util.Util;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button resetLoginBtn = (Button) findViewById(R.id.resetLoginBtn);
        resetLoginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Util.deleteToken(MainActivity.this);
                Toast.makeText(getApplicationContext(), "Deleted login token", Toast.LENGTH_SHORT).show();
            }
        });

        Button startServiceBtn = (Button) findViewById(R.id.startService);
        startServiceBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Util.registerOutgoingSmsListener(getApplicationContext());
                Util.setupPusher(getApplicationContext());
                Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_SHORT).show();
            }
        });
    }


}