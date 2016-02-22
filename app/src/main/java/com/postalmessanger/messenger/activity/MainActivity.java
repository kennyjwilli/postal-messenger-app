package com.postalmessanger.messenger.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.postalmessanger.messenger.R;
import com.postalmessanger.messenger.data_representation.Contact;
import com.postalmessanger.messenger.db.DbUtil;
import com.postalmessanger.messenger.util.SLAPI;
import com.postalmessanger.messenger.util.Util;

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
                Util.setupPusher(getApplicationContext());
                Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_SHORT).show();
            }
        });

        Button printContactsBtn = (Button) findViewById(R.id.getContacts);
        printContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("PostalMessenger", Util.contactsJson(Util.getContacts(getApplicationContext())));
            }
        });

        Button testBtn = (Button) findViewById(R.id.testBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("PostalMessenger", "before" + String.valueOf(DbUtil.hasMessage(DbUtil.getReadableDb(getApplicationContext()), "content://sms/329")));
                DbUtil.insertMessage(DbUtil.getWriteableDb(getApplicationContext()), "content://sms/329");
                Log.v("PostalMessenger", "after" + String.valueOf(DbUtil.hasMessage(DbUtil.getReadableDb(getApplicationContext()), "content://sms/329")));
            }
        });
    }


}