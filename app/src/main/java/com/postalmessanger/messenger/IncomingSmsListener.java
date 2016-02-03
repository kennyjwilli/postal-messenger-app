package com.postalmessanger.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.postalmessanger.messenger.data_representation.Event;
import com.postalmessanger.messenger.data_representation.Message;
import com.postalmessanger.messenger.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
                    SmsMessage msg = msgs[i];
                    msgFrom = msg.getOriginatingAddress();
                    String msgBody = msg.getMessageBody();
                    Log.v("PostalMessenger", "FROM "+msgFrom);
                    Log.v("PostalMessenger", "MESSAGE BODY " + msgBody);
                    Event evt = new Event("client", "add-message", new Message("received", msg.getTimestampMillis(), msgFrom, msgBody));
                    try
                    {
                        Util.sendAddMessageEvent(context, evt, new Callback()
                        {
                            @Override
                            public void onFailure(Call call, IOException e)
                            {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException
                            {
                                if(response.isSuccessful())
                                {
                                    Log.v("PostalMessenger", "Added message successfully!");
                                }else
                                {
                                    Log.v("PostalMessenger", "Failed to add message");
                                }
                                response.body().close();
                            }
                        });
                    } catch (JSONException | IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }else
            {
                Log.v("PostalMessenger", "PDUs is NULL");
            }
        }
    }
}
