package com.postalmessanger.messenger.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.postalmessanger.messenger.R;
import com.postalmessanger.messenger.util.Http;
import com.postalmessanger.messenger.util.SLAPI;
import com.postalmessanger.messenger.util.Util;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SLAPI.hasToken(this)) {
            startMainActivity();
            return;
        }
        setContentView(R.layout.activity_login);

        final EditText usernameTxt = (EditText) findViewById(R.id.usernameText);
        final EditText passwordTxt = (EditText) findViewById(R.id.passwordText);

        final Button loginBtn = (Button) findViewById(R.id.loginButton);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (usernameTxt.getText().toString().isEmpty() || passwordTxt.getText().toString().isEmpty()) {
                    return;
                }
                //Show loading dialog
                final ProgressDialog dialog = new ProgressDialog(LoginActivity.this, ProgressDialog.STYLE_SPINNER);
                dialog.setIndeterminate(true);
                dialog.setMessage("Logging you in...");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                //POST to creds to /login to get JWT
                Gson gson = new Gson();
                Map<String, String> json = new HashMap<>();
                json.put("username", usernameTxt.getText().toString());
                json.put("password", passwordTxt.getText().toString());
                try {
                    Http.post(Http.BASE_URL + "/login", gson.toJson(json), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.v("PostalMessenger", "Error!!!!!!");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful())
                                throw new IOException("Unexpected code " + response.code());
                            String token = response.body().string();
                            SLAPI.saveToken(LoginActivity.this, token);
                            requestPusherInfo(token, dialog);
                            response.body().close();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestPusherInfo(String token, final Dialog dialog) {
        Http.get(Http.BASE_URL + "/api/pusher", Http.getAuthHeaders(token), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Gson gson = new Gson();
                    Type type = Util.getStringStringType();
                    Map<String, String> json = gson.fromJson(body, type);
                    SLAPI.saveValues(getApplicationContext(), json);
                    closeDialogAndStartApp(dialog);
                }
                response.body().close();
            }
        });
    }

    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    private void closeDialogAndStartApp(final Dialog dialog) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                startMainActivity();
                finish();
            }
        });
    }
}