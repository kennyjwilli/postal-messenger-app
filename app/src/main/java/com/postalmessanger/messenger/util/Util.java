package com.postalmessanger.messenger.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.postalmessanger.messenger.OutgoingSmsHandler;
import com.postalmessanger.messenger.data_representation.Event;
import com.postalmessanger.messenger.data_representation.Json;
import com.postalmessanger.messenger.data_representation.Message;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by kenny on 1/31/16.
 */
public class Util {
    public static String socket_id;
    public static final Uri OUTGOING_SMS_URI = Uri.parse("content://sms");

    public static void registerOutgoingSmsListener(Context ctx) {
        ContentResolver resolver = ctx.getContentResolver();
        resolver.registerContentObserver(OUTGOING_SMS_URI, true, new OutgoingSmsHandler(new Handler(), ctx));
    }

    public static Type getStringStringType() {
        return new TypeToken<Map<String, String>>() {
        }.getType();
    }

    public static String md5(String in) throws NoSuchAlgorithmException {
        MessageDigest digest;
        digest = MessageDigest.getInstance("MD5");
        digest.reset();
        digest.update(in.getBytes());
        byte[] a = digest.digest();
        int len = a.length;
        StringBuilder sb = new StringBuilder(len << 1);
        for (byte anA : a) {
            sb.append(Character.forDigit((anA & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(anA & 0x0f, 16));
        }
        return sb.toString();
    }

    public static void sendSMS(Context ctx, Message msg, final Fn fn) {
        final String SENT = "SMS_SENT";
        SmsManager smsManager = SmsManager.getDefault();
        final String recipients = msg.recipients.get(0);
        final String message = msg.data;
        Intent sentIntent = new Intent(SENT);
        PendingIntent piSent = PendingIntent.getBroadcast(ctx, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        BroadcastReceiver sentBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.v("PostalMessenger", "action " + action);
                if (action != null && action.equals(SENT)) {
                    if (getResultCode() == Activity.RESULT_OK) {
                        fn.onSuccess();
                        Log.v("PostalMessenger", "sent message successfully");
                    } else {
                        fn.onError();
                        Log.v("PostalMessenger", "failed to send message");
                    }
                }
            }
        };
        ctx.registerReceiver(sentBroadcastReceiver, new IntentFilter(SENT));
        smsManager.sendTextMessage(recipients, null, message, piSent, null);
    }

    public static void setupPusher(final Context ctx) {
        HttpAuthorizer authorizer = new HttpAuthorizer(Http.BASE_URL + "/api/pusher-auth");
        authorizer.setHeaders(Http.getAuthHeaders(ctx));
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        //TODO: Get API_KEY from server
        final Pusher pusher = new Pusher("d24a197fd369b0ed0b58", options);
        //TODO: Pusher may lose connection. See link
        //also verify changing from lte to wifi wont drop connection
        //https://github.com/pusher/pusher-websocket-java/issues/34#issuecomment-160173016
        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.v("PostalMessenger", "State changed from " + change.getPreviousState() +
                        " to " + change.getCurrentState());
                if (change.getCurrentState().equals(ConnectionState.CONNECTED)) {
                    socket_id = pusher.getConnection().getSocketId();
                }
            }

            @Override
            public void onError(String message, String code, Exception e) {
                //TODO: Add pusher reconnect code
                //https://github.com/pusher/pusher-websocket-java/issues/34#issuecomment-39449068
                Log.e("PostalMessenger", "There was a problem connecting! " + message + " " + code);
            }
        }, ConnectionState.ALL);
        PrivateChannel channel = pusher.subscribePrivate("private-message-john@example.com");
        channel.bind("messages", new PrivateChannelEventListener() {
            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                Log.e("PostalMessenger", String.format("Authentication failure due to [%s], exception was [%s]", message, e));
            }

            @Override
            public void onSubscriptionSucceeded(String s) {
                Log.v("PostalMessenger", "Subscription succeeded");
            }

            @Override
            public void onEvent(String channelName, String eventName, String data) {
                final Event evt = Json.fromJson(data);
                if (evt.dest.equals("phone")) {
                    switch (evt.type) {
                        case "send-message":
                            Log.v("PostalMessenger", "Send " + evt.message);
                            sendSMS(ctx, evt.message, new Fn() {
                                @Override
                                public void onSuccess() {
                                    try {
                                        sendEvent(ctx, markMessageSentJson(evt), new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                            }
                                        });
                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError() {

                                }
                            });
                        default:
                    }
                    Log.v("PostalMessenger", "parsed " + evt);
                }
            }
        });
    }

    public static final String ADD_MESSAGE = "add-message";
    public static final String MARK_AS_SENT = "mark-as-sent";
    public static final String SMS_RECEIVED = "received";
    public static final String SMS_SENT = "sent";

    public static JsonArray toJsonArray(List<String> list) {
        JsonArray arr = new JsonArray();
        for (String s : list) {
            arr.add(s);
        }
        return arr;
    }

    public static String normalizePhoneNumber(String number) {
        return number.replaceAll("[^\\d]", "");
    }

    public static List<String> normalizeRecipients(List<String> recipients) {
        List<String> result = new ArrayList<>();
        for (String s : recipients) {
            result.add(normalizePhoneNumber(s));
        }
        return result;
    }

    public static String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date(timestamp));
    }

    public static String markMessageSentJson(Event evt) {
        JsonObject json = new JsonObject();
        json.addProperty("dest", "phone");
        json.addProperty("type", MARK_AS_SENT);
        json.addProperty("socket_id", socket_id);
        json.add("message", new Gson().toJsonTree(evt.message));
        return new Gson().toJson(json);
    }

    public static String addMessageJson(String type, List<String> recipients, long timestamp, String data) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);
        normalizeRecipients(recipients);
        msg.add("recipients", toJsonArray(normalizeRecipients(recipients)));
        msg.addProperty("timestamp", formatTimestamp(timestamp));
        msg.addProperty("data", data);

        JsonObject json = new JsonObject();
        json.addProperty("dest", "client");
        json.addProperty("type", ADD_MESSAGE);
        json.addProperty("socket_id", socket_id);
        json.add("message", msg);
        return new Gson().toJson(json);
    }

    public static void sendEvent(Context ctx, String json, Callback callback) throws JSONException, IOException {
        Log.v("PostalMessenger", "Sent " + json);
        Http.post(Http.BASE_URL + "/api/message", Http.getAuthHeaders(ctx.getApplicationContext()), json, callback);
    }
}