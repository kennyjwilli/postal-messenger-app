package com.postalmessanger.messenger.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.postalmessanger.messenger.OutgoingSmsHandler;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kenny on 1/31/16.
 */
public class Util
{
    public static final String settingsPrefName = "settings";

    public static String getToken(Context ctx)
    {
        SharedPreferences prefs = ctx.getSharedPreferences(settingsPrefName, Context.MODE_PRIVATE);
        return prefs.getString("Token", null);
    }

    public static boolean hasToken(Activity a)
    {
        return getToken(a) != null;
    }

    public static void saveToken(Context ctx, String token)
    {
        SharedPreferences prefs = ctx.getSharedPreferences(settingsPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Token", token);
        editor.apply();
    }

    public static void deleteToken(Context ctx)
    {
        SharedPreferences prefs = ctx.getSharedPreferences(settingsPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("Token");
        editor.apply();
    }

    public static void registerOutgoingSmsListener(Context ctx)
    {
        ContentResolver resolver = ctx.getContentResolver();
        resolver.registerContentObserver(Uri.parse("content://sms"), true, new OutgoingSmsHandler(new Handler(), resolver));
    }

    public static void setupPusher(Context ctx)
    {
        HttpAuthorizer authorizer = new HttpAuthorizer(Http.BASE_URL + "/api/pusher-auth");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Token " + getToken(ctx));
        authorizer.setHeaders(headers);
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        //TODO: Get API_KEY from server
        Pusher pusher = new Pusher("d24a197fd369b0ed0b58", options);
        pusher.connect(new ConnectionEventListener()
        {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change)
            {
                Log.v("PostalMessenger", "State changed to " + change.getCurrentState() +
                        " from " + change.getPreviousState());
            }

            @Override
            public void onError(String message, String code, Exception e)
            {
                Log.e("PostalMessenger", "There was a problem connecting! " + message + " " + code);
            }
        }, ConnectionState.ALL);
        PrivateChannel channel = pusher.subscribePrivate("private-message-john@example.com");
        channel.bind("messages", new PrivateChannelEventListener()
        {
            @Override
            public void onAuthenticationFailure(String message, Exception e)
            {
                Log.e("PostalMessenger", String.format("Authentication failure due to [%s], exception was [%s]", message, e));
            }

            @Override
            public void onSubscriptionSucceeded(String s)
            {
                Log.v("PostalMessenger", "Subscription succeeded");
            }

            @Override
            public void onEvent(String channelName, String eventName, String data)
            {
                Log.v("PostalMessenger", "Got: " + data);
            }
        });
    }
}