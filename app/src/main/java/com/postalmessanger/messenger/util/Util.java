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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.postalmessanger.messenger.OutgoingSmsHandler;
import com.postalmessanger.messenger.Pusher;
import com.postalmessanger.messenger.data_representation.Contact;
import com.postalmessanger.messenger.data_representation.Message;
import com.postalmessanger.messenger.data_representation.PNumber;
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
import java.util.Locale;
import java.util.Map;

import okhttp3.Callback;


/**
 * Created by kenny on 1/31/16.
 */
public class Util {
    public static final Uri OUTGOING_SMS_URI = Uri.parse("content://sms");

    public static void initService(Context ctx) {
        registerOutgoingSmsListener(ctx);
        Pusher.init(ctx).start();
        Toast.makeText(ctx, "Registered", Toast.LENGTH_SHORT).show();
    }

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
                long timestamp = cur.getLong(cur.getColumnIndex("date"));
                cur.close();
                result = new Message(null, Collections.singletonList(number), timestamp, text);
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

    public static List<PNumber> phoneNumbersFor(Context ctx, String id) {
        List<PNumber> phoneNumbers = new ArrayList<>();
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
                phoneNumbers.add(new PNumber(type, phoneNumber));
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
                List<PNumber> phoneNumbers = new ArrayList<>();
                if (hasPhoneNumber(cur)) {
                    phoneNumbers = phoneNumbersFor(ctx, id);
                }
                if (!phoneNumbers.isEmpty()) {
                    contacts.add(new Contact(id, name, phoneNumbers));
                }
            }
            cur.close();
        }
        return contacts;
    }

    public static List<String> getRecipients(Context ctx, List<String> recip_ids) {
        String where = "";
        for (int i = 0; i < recip_ids.size(); i++) {
            where += "_id = " + recip_ids.get(i);
            if (i != recip_ids.size() - 1) {
                where += " OR ";
            }
        }
        Uri uri = Uri.parse("content://mms-sms/canonical-addresses");
        String[] projection = new String[]{"address"};
        Cursor cur = ctx.getContentResolver().query(uri, projection, where, null, null);
        List<String> addresses = null;
        if (cur != null && cur.getCount() > 0) {
            addresses = new ArrayList<>();
            while (cur.moveToNext()) {
                addresses.add(normalizePhoneNumber(cur.getString(cur.getColumnIndex("address"))));
            }
            cur.close();
        }
        return addresses;
    }

    public static int getMessageNumber(String uri) {
        String[] split = uri.split("/");
        return Integer.parseInt(split[split.length - 1]);
    }

    public static String normalizePhoneNumber(String number) {
        PhoneNumberUtil putil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber proto = putil.parse(number, Locale.getDefault().getCountry());
            return putil.format(proto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return number.replaceAll("[^0-9]", "");
    }

    public static List<String> normalizePhoneNumbers(List<String> recipients) {
        List<String> result = new ArrayList<>();
        for (String s : recipients) {
            result.add(normalizePhoneNumber(s));
        }
        return result;
    }

    public static String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date(timestamp));
    }

    public static void sendEvent(Context ctx, String type, JsonElement data) throws IOException, JSONException {
        JsonObject json = new JsonObject();
        json.addProperty("d", "client");
        json.addProperty("t", type);
        json.add("b", data);
        push(ctx, json);
    }

    public static void push(Context ctx, JsonElement obj, Callback callback) throws JSONException, IOException {
        JsonObject json = new JsonObject();
        json.addProperty("socket_id", Pusher.getInstance().getSocket_id());
        json.add("body", obj);
        Gson gson = new GsonBuilder()
                .create();
        String send = gson.toJson(json);
        Log.v("PostalMessenger", "Sent " + send);
        Http.post(Http.BASE_URL + "/api/message", Http.getAuthHeaders(ctx.getApplicationContext()), send, callback);
    }

    public static void push(Context ctx, JsonElement json) throws IOException, JSONException {
        push(ctx, json, Http.emptyCallback());
    }
}