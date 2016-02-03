package com.postalmessanger.messenger;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.postalmessanger.messenger.data_representation.Event;
import com.postalmessanger.messenger.data_representation.Message;
import com.postalmessanger.messenger.util.Util;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by kenny on 2/1/16.
 */
public class OutgoingSmsHandler extends ContentObserver
{
    private Context ctx;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public OutgoingSmsHandler(Handler handler, Context ctx)
    {
        super(handler);
        this.ctx = ctx;
    }

    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);
        Uri uriSMSURI = Uri.parse("content://sms");
        ContentResolver resolver = ctx.getContentResolver();
        Cursor cur = resolver.query(uriSMSURI, null, null, null, null);
        if (cur != null)
        {
            cur.moveToNext();
            String smsBody = cur.getString(cur.getColumnIndex("body"));
            String smsNumber = cur.getString(cur.getColumnIndex("address"));
            long date = cur.getLong(cur.getColumnIndex("date"));
            int type = cur.getInt(cur.getColumnIndex("type"));

            //TODO: Implement more types. See URL below
            //http://stackoverflow.com/questions/15352103/android-documentation-for-content-sms-type-values
            if (type == 2)
            {
                Event evt = new Event("client", "add-message", new Message("sent", date, smsNumber, smsBody));
                try
                {
                    Util.sendAddMessageEvent(ctx, evt, new Callback()
                    {
                        @Override
                        public void onFailure(Call call, IOException e)
                        {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException
                        {
                            if (response.isSuccessful())
                            {
                                Log.v("PostalMessenger", "Added sent message successfully!");
                            } else
                            {
                                Log.v("PostalMessenger", "Failed to add sent message");
                            }
                            response.body().close();
                        }
                    });
                } catch (JSONException | IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
