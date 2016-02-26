package com.postalmessanger.messenger.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.postalmessanger.messenger.Pusher;
import com.postalmessanger.messenger.R;
import com.postalmessanger.messenger.data_representation.Json;
import com.postalmessanger.messenger.data_representation.Message;
import com.postalmessanger.messenger.enums.EventType;
import com.postalmessanger.messenger.enums.MessageType;
import com.postalmessanger.messenger.util.SLAPI;
import com.postalmessanger.messenger.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button resetLoginBtn = (Button) findViewById(R.id.resetLoginBtn);
        resetLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SLAPI.deleteToken(MainActivity.this);
                Toast.makeText(getApplicationContext(), "Deleted login token", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        Button startServiceBtn = (Button) findViewById(R.id.startService);
        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.registerOutgoingSmsListener(getApplicationContext());
                Pusher.init(getApplicationContext()).start();
                Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_SHORT).show();
            }
        });

        Button printContactsBtn = (Button) findViewById(R.id.getContacts);
        printContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v("PostalMessenger", Util.contactsJson(Util.getContacts(getApplicationContext())));
            }
        });

        Button testBtn = (Button) findViewById(R.id.testBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Util.sendEvent(getApplicationContext(), EventType.MESSAGE_SENT, Json.toGson().toJsonTree(new Message("sent", Collections.singletonList("9252196640"), 1455874149684L, "the body")));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}