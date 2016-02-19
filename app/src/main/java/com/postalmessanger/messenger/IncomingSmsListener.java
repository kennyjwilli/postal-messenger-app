package com.postalmessanger.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.postalmessanger.messenger.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by kenny on 1/31/16.
 */
public class IncomingSmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String format = bundle.getString("format");
            String msgFrom;
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    //TODO: Use format
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    } else {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    SmsMessage msg = msgs[i];
                    msgFrom = msg.getOriginatingAddress();
                    String msgBody = msg.getMessageBody();
                    String json = Util.addMessageJson(Util.SMS_RECEIVED, Collections.singletonList(msgFrom), msg.getTimestampMillis(), msgBody);
                    try {
                        Util.sendEvent(context, json, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    Log.v("PostalMessenger", "Added message successfully!");
                                } else {
                                    Log.v("PostalMessenger", "Failed to add message");
                                }
                                response.body().close();
                            }
                        });
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.v("PostalMessenger", "PDUs is NULL");
            }
        }
    }
}
