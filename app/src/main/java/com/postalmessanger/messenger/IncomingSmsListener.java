package com.postalmessanger.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by kenny on 1/31/16.
 */
public class IncomingSmsListener extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            Log.v("PostalMessenger", "GOT SMS");
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String format = bundle.getString("format");
            String msgFrom;
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null)
            {
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++)
                {
                    //TODO: Use format
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    }else
                    {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    msgFrom = msgs[i].getOriginatingAddress();
                    String msgBody = msgs[i].getMessageBody();
                    Log.v("PostalMessenger", "FROM "+msgFrom);
                    Log.v("PostalMessenger", "MESSAGE BODY " + msgBody);
                }
            }else
            {
                Log.v("PostalMessenger", "PDUs is NULL");
            }
        }
    }
}
