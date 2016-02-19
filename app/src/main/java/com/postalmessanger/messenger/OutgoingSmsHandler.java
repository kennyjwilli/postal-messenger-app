package com.postalmessanger.messenger;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import com.postalmessanger.messenger.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by kenny on 2/1/16.
 */
public class OutgoingSmsHandler extends ContentObserver {
    private Context ctx;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public OutgoingSmsHandler(Handler handler, Context ctx) {
        super(handler);
        this.ctx = ctx;
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        ContentResolver resolver = ctx.getContentResolver();
        Cursor cur = resolver.query(Util.OUTGOING_SMS_URI, null, null, null, null);
        if (cur != null) {
            cur.moveToNext();
            String smsBody = cur.getString(cur.getColumnIndex("body"));
            String smsNumber = cur.getString(cur.getColumnIndex("address"));
            long timestamp = cur.getLong(cur.getColumnIndex("date"));
            int type = cur.getInt(cur.getColumnIndex("type"));
            Log.v("PostalMessenger", "change " + type);

            //TODO: Implement more types. See URL below
            //http://stackoverflow.com/questions/15352103/android-documentation-for-content-sms-type-values
            if (type == 2) {
                String json = Util.addMessageJson(Util.SMS_SENT, Collections.singletonList(smsNumber), timestamp, smsBody);
                try {
                    Util.sendEvent(ctx, json, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                Log.v("PostalMessenger", "Added sent message successfully!");
                            } else {
                                Log.v("PostalMessenger", "Failed to add sent message");
                            }
                            response.body().close();
                        }
                    });
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
