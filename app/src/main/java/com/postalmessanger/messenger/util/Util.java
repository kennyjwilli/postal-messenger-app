package com.postalmessanger.messenger.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.postalmessanger.messenger.OutgoingSmsHandler;
import com.postalmessanger.messenger.Pusher;
import com.postalmessanger.messenger.data_representation.Contact;
import com.postalmessanger.messenger.data_representation.Message;
import com.postalmessanger.messenger.data_representation.PhoneNumber;
import com.postalmessanger.messenger.db.DbUtil;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;


/**
 * Created by kenny on 1/31/16.
 */
public class Util {
    public static final Uri OUTGOING_SMS_URI = Uri.parse("content://sms");

    public static void registerOutgoingSmsListener(Context ctx) {
        ContentResolver resolver = ctx.getContentResolver();
        resolver.registerContentObserver(OUTGOING_SMS_URI, true, new OutgoingSmsHandler(new Handler(), ctx));
    }

    public static Type getStringStringType() {
        return new TypeToken<Map<String, String>>() {}.getType();
    }

    public static void printExtras(Bundle bundle) {
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.d("PostalMessenger", String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
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

    public static Message getMessage(Context ctx, Uri uri) {
        Cursor cur = ctx.getContentResolver().query(uri, null, null, null, null);
        Message result = null;
        if (cur != null) {
            if (cur.moveToFirst() && cur.getCount() > 0) {
                String text = cur.getString(cur.getColumnIndex("body"));
                String number = cur.getString(cur.getColumnIndex("address"));
                long date = cur.getLong(cur.getColumnIndex("date"));
                cur.close();
                result = new Message(null, Collections.singletonList(number), formatTimestamp(date), text);
            }
        }
        return result;
    }

    public static void sendSMS(final Context ctx, Message msg, final Fn fn) {
        final String SENT = "SMS_SENT";
        SmsManager smsManager = SmsManager.getDefault();
        final String number = msg.recipients.get(0);
        final String text = msg.text;
        Intent sentIntent = new Intent(SENT);
        PendingIntent piSent = PendingIntent.getBroadcast(ctx, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        BroadcastReceiver sentBroadcastReceiver = new BroadcastReceiver() {
            boolean hasSent = false;

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(SENT)) {
                    if (getResultCode() == Activity.RESULT_OK && !hasSent) {
                        String uri = (String) intent.getExtras().get("uri");
                        DbUtil.insertMessage(ctx, uri);
                        fn.onSuccess(uri);
                        hasSent = true;
                    } else {
                        fn.onError();
                        Log.v("PostalMessenger", "failed to send message");
                    }
                }
            }
        };
        ctx.registerReceiver(sentBroadcastReceiver, new IntentFilter(SENT));
        smsManager.sendTextMessage(number, null, text, piSent, null);
    }

    public static String getPhoneNumberTypeString(int type) {
        String sType = "";
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                sType = "Home";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                sType = "Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                sType = "Work";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                sType = "Home Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                sType = "Work Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                sType = "Main";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                sType = "Other";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                sType = "Custom";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                sType = "Pager";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
                sType = "Assistant";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
                sType = "Callback";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
                sType = "Car";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
                sType = "Company Main";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
                sType = "ISDN";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
                sType = "MMS";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
                sType = "Other Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
                sType = "Radio";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
                sType = "Telex";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
                sType = "TTY TDD";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                sType = "Work Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
                sType = "Work Pager";
                break;
        }
        return sType;
    }

    public static List<PhoneNumber> phoneNumbersFor(Context ctx, String id) {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        ContentResolver cr = ctx.getContentResolver();
        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{id}, null);
        if (pCur != null) {
            while (pCur.moveToNext()) {
                int type = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumbers.add(new PhoneNumber(getPhoneNumberTypeString(type), phoneNumber));
            }
            pCur.close();
        }
        return phoneNumbers;
    }

    public static boolean hasPhoneNumber(Cursor cur) {
        return 0 < Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
    }

    public static List<Contact> getContacts(Context ctx) {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver cr = ctx.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur != null) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(Contact.CONTACT_NAME));
                List<PhoneNumber> phoneNumbers = new ArrayList<>();
                if (hasPhoneNumber(cur)) {
                    phoneNumbers = phoneNumbersFor(ctx, id);
                }
                contacts.add(new Contact(id, name, phoneNumbers));
            }
            cur.close();
        }
        return contacts;
    }

    public static int getMessageNumber(String uri) {
        String[] split = uri.split("/");
        return Integer.parseInt(split[split.length - 1]);
    }

    public static final String ADD_MESSAGE = "add-message";
    public static final String MESSAGE_SENT = "message-sent";
    public static final String SMS_RECEIVED = "received";
    public static final String SMS_SENT = "sent";

    public static String normalizePhoneNumber(String number) {
        return number.replaceAll("[^\\d]", "");
    }

    public static List<String> normalizePhoneNumbers(List<String> recipients) {
        List<String> result = new ArrayList<>();
        for (String s : recipients) {
            result.add(normalizePhoneNumber(s));
        }
        return result;
    }

    public static void normalizeContacts(List<Contact> contacts) {
        for (Contact c : contacts) {
            //c.phoneNumbers = normalizePhoneNumbers(c.phoneNumbers);
        }
    }

    public static String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date(timestamp));
    }

    public static JsonObject sendClientEvent(String type, JsonElement data) {
        JsonObject json = new JsonObject();
        json.addProperty("dest", "client");
        json.addProperty("socket_id", Pusher.getInstance().getSocket_id());
        json.addProperty("type", type);
        json.add("data", data);
        return json;
    }

    public static String contactsJson(List<Contact> contacts) {
        JsonObject json = sendClientEvent("get-contacts", new Gson().toJsonTree(contacts));
        return new Gson().toJson(json);
    }

    public static String messageSentJson(Message msg) {
        JsonObject json = sendClientEvent(MESSAGE_SENT, new Gson().toJsonTree(msg));
        return new Gson().toJson(json);
    }

    public static String addMessageJson(String type, List<String> recipients, long timestamp, String text) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);
        msg.add("recipients", new Gson().toJsonTree(normalizePhoneNumbers(recipients)));
        msg.addProperty("timestamp", formatTimestamp(timestamp));
        msg.addProperty("text", text);

        JsonObject json = sendClientEvent(ADD_MESSAGE, msg);
        return new Gson().toJson(json);
    }

    public static void sendEvent(Context ctx, String json, Callback callback) throws JSONException, IOException {
        Log.v("PostalMessenger", "Sent " + json);
        Http.post(Http.BASE_URL + "/api/message", Http.getAuthHeaders(ctx.getApplicationContext()), json, callback);
    }

    public static void sendEvent(Context ctx, String json) throws IOException, JSONException {
        sendEvent(ctx, json, Http.emptyCallback());
    }
}