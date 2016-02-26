package com.postalmessanger.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.postalmessanger.messenger.data_representation.Json;
import com.postalmessanger.messenger.data_representation.Message;
import com.postalmessanger.messenger.enums.EventType;
import com.postalmessanger.messenger.enums.MessageType;
import com.postalmessanger.messenger.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by kenny on 1/31/16.
 */
public class IncomingSmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
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
                    Message message = new Message(MessageType.SMS_RECEIVED, Collections.singletonList(msgFrom), msg.getTimestampMillis(), msgBody);
                    try {
                        Util.sendEvent(ctx, EventType.ADD_MESSAGE, Json.toJson(message));
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.v("PostalMessenger", "PDUs is NULL");
            }
        }
    }
}
