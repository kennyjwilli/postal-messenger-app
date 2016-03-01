package com.postalmessanger.messenger;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.postalmessanger.messenger.data_representation.Contact;
import com.postalmessanger.messenger.data_representation.Conversation;
import com.postalmessanger.messenger.data_representation.ConversationSnippet;
import com.postalmessanger.messenger.data_representation.Event;
import com.postalmessanger.messenger.data_representation.Json;
import com.postalmessanger.messenger.data_representation.Message;
import com.postalmessanger.messenger.db.DbUtil;
import com.postalmessanger.messenger.enums.EventType;
import com.postalmessanger.messenger.util.Fn;
import com.postalmessanger.messenger.util.Http;
import com.postalmessanger.messenger.util.SLAPI;
import com.postalmessanger.messenger.util.Util;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kenny on 2/2/16.
 */
public class Pusher {
    private static Pusher pusher;
    private Context ctx;
    private String socket_id;

    private Pusher(Context ctx) {
        this.ctx = ctx;
    }

    public static Pusher init(Context ctx) {
        if (pusher == null) {
            pusher = new Pusher(ctx);
        }
        return pusher;
    }

    public static Pusher getInstance() {
        return pusher;
    }

    public String getSocket_id() {
        return this.socket_id;
    }

    public void start() {
        HttpAuthorizer authorizer = new HttpAuthorizer(Http.BASE_URL + "/api/pusher-auth");
        authorizer.setHeaders(Http.getAuthHeaders(ctx));
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        final com.pusher.client.Pusher pusher = new com.pusher.client.Pusher(SLAPI.getSavedValue(ctx, SLAPI.API_KEY), options);
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

        PrivateChannel channel = pusher.subscribePrivate(SLAPI.getSavedValue(ctx, SLAPI.MESSAGE_CHANNEL));
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
                Log.v("PostalMessenger", "RECEIVED " + data);
                final JsonObject jsonEvent = new JsonParser().parse(data).getAsJsonObject();
                if (jsonEvent.get("dest").getAsString().equals("phone")) {
                    switch (jsonEvent.get("type").getAsString()) {
                        case "send-message":
                            final Event evt = new Gson().fromJson(jsonEvent, Event.class);
                            handleSendMessage(evt);
                            break;
                        case "get-contacts":
                            handleGetContacts();
                            break;
                        case "get-conversations":
                            handleGetConversations();
                            break;
                        case "get-conversation":
                            handleGetConversation(jsonEvent);
                            break;
                        default:
                    }
                }
            }
        });
    }

    private void handleSendMessage(final Event evt) {
        Log.v("PostalMessenger", "Send " + evt.data);
        final int id = DbUtil.getNextSmsId(ctx);
        // TODO: Not sure if marking message before it is sent is really going to fly
        DbUtil.insertMessage(ctx, id);
        Log.v("PostalMessenger", "event idx " + evt.data.idx);
        Util.sendSMS(ctx, evt.data, new Fn() {
            @Override
            public void onSuccess(Object... args) {
                String uri = (String) args[0];
                Message msg = Util.getMessage(ctx, Uri.parse(uri));
                if (msg != null) {
                    msg.idx = evt.data.idx;
                    try {
                        Util.sendEvent(ctx, EventType.MESSAGE_SENT, Json.toJson(msg));
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError() {
                DbUtil.removeMessage(ctx, id);
            }
        });
    }

    private void handleGetContacts() {
        List<Contact> contacts = Util.getContacts(ctx);
        try {
            Util.sendEvent(ctx, EventType.GET_CONTACTS, Json.toJson(contacts));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleGetConversations() {
        Uri uri = Uri.parse("content://mms-sms/conversations");
        final String[] projection = new String[]{"thread_id", "date", "address", "body"};
        Cursor cur = ctx.getContentResolver().query(uri, projection, null, null, "date DESC");
        if (cur != null) {
            List<ConversationSnippet> convs = new ArrayList<>();
            while (cur.moveToNext()) {
                String thread_id = cur.getString(cur.getColumnIndex("thread_id"));
                long timestamp = cur.getLong(cur.getColumnIndex("date"));
                String address = cur.getString(cur.getColumnIndex("address"));
                String text = cur.getString(cur.getColumnIndex("body"));
                if (address != null) {
                    convs.add(new ConversationSnippet(thread_id, timestamp, Util.formatPhoneNumber(ctx, address), text));
                }
            }
            cur.close();
            try {
                Util.sendEvent(ctx, EventType.GET_CONVERSATIONS, Json.toJson(convs));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleGetConversation(JsonObject evt) {
        String thread_id = evt.get("data").getAsJsonObject().get("thread_id").getAsString();
        Uri uri = Uri.parse("content://sms");
        final String[] projection = new String[]{"type", "address", "date", "body"};
        Cursor cur = ctx.getContentResolver().query(uri, projection, "thread_id=?", new String[]{thread_id}, "date ASC");
        if (cur != null) {
            List<Message> messages = new ArrayList<>();
            Set<String> recipients = new HashSet<>();
            while (cur.moveToNext()) {
                int type = cur.getInt(cur.getColumnIndex("type"));
                String address = cur.getString(cur.getColumnIndex("address"));
                long timestamp = cur.getLong(cur.getColumnIndex("date"));
                String text = cur.getString(cur.getColumnIndex("body"));
                messages.add(new Message(type, timestamp, text));
                recipients.add(Util.normalizePhoneNumber(address));
            }
            cur.close();
            Conversation conv = new Conversation(thread_id, new ArrayList<>(recipients), messages);
            try {
                Util.sendEvent(ctx, EventType.GET_CONVERSATION, Json.toJson(conv));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
