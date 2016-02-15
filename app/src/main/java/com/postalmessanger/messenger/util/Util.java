package com.postalmessanger.messenger.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.postalmessanger.messenger.OutgoingSmsHandler;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;


/**
 * Created by kenny on 1/31/16.
 */
public class Util
{
    public static String socket_id;

    public static void registerOutgoingSmsListener(Context ctx)
    {
        ContentResolver resolver = ctx.getContentResolver();
        resolver.registerContentObserver(Uri.parse("content://sms"), true, new OutgoingSmsHandler(new Handler(), ctx));
    }

    public static Type getStringStringType()
    {
        return new TypeToken<Map<String, String>>()
        {
        }.getType();
    }

    public static void setupPusher(Context ctx)
    {
        HttpAuthorizer authorizer = new HttpAuthorizer(Http.BASE_URL + "/api/pusher-auth");
        authorizer.setHeaders(Http.getAuthHeaders(ctx));
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        //TODO: Get API_KEY from server
        final Pusher pusher = new Pusher("d24a197fd369b0ed0b58", options);
        pusher.connect(new ConnectionEventListener()
        {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change)
            {
                Log.v("PostalMessenger", "State changed to " + change.getCurrentState() +
                        " from " + change.getPreviousState());
                if (change.getCurrentState().equals(ConnectionState.CONNECTED))
                {
                    socket_id = pusher.getConnection().getSocketId();
                }
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
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                //problem is with nested json parsing to object. see link
                //http://stackoverflow.com/questions/23919605/translating-nested-java-objects-to-from-json-using-gson
                Log.v("PostalMessenger", data);
                //Event e = gson.fromJson(json, Event.class);
                //Log.v("PostalMessenger", "Got: " + map);
            }
        });
    }

    public static final String ADD_MESSAGE = "add-message";
    public static final String SMS_RECEIVED = "received";
    public static final String SMS_SENT = "sent";

    public static JsonArray toJsonArray(List<String> list)
    {
        JsonArray arr = new JsonArray();
        for (String s : list)
        {
            arr.add(s);
        }
        return arr;
    }

    public static String normalizePhoneNumber(String number)
    {
        return number.replaceAll("[^\\d]", "");
    }

    public static List<String> normalizeRecipients(List<String> recipients)
    {
        List<String> result = new ArrayList<>();
        for (String s : recipients)
        {
            result.add(normalizePhoneNumber(s));
        }
        return result;
    }

    public static String addMessageJson(String type, List<String> recipients, long timestamp, String data)
    {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);
        normalizeRecipients(recipients);
        msg.add("recipients", toJsonArray(normalizeRecipients(recipients)));
        msg.addProperty("timestamp", timestamp);
        msg.addProperty("data", data);

        JsonObject json = new JsonObject();
        json.addProperty("dest", "client");
        json.addProperty("type", ADD_MESSAGE);
        json.addProperty("socket_id", socket_id);
        json.add("message", msg);
        return new Gson().toJson(json);
    }

    public static void sendAddMessageEvent(Context ctx, String json, Callback callback) throws JSONException, IOException
    {
        Log.v("PostalMessenger", "Sent "+json);
        Http.post(Http.BASE_URL + "/api/message", Http.getAuthHeaders(ctx.getApplicationContext()), json, callback);
    }
}