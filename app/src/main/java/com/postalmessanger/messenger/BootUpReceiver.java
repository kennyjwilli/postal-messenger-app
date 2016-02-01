package com.postalmessanger.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.postalmessanger.messenger.util.Util;

/**
 * Created by kenny on 2/1/16.
 */
public class BootUpReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Util.registerOutgoingSmsListener(context);
    }
}