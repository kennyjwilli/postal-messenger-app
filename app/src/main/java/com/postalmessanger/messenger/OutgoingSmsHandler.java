package com.postalmessanger.messenger;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by kenny on 2/1/16.
 */
public class OutgoingSmsHandler extends ContentObserver
{
    private ContentResolver contentResolver;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public OutgoingSmsHandler(Handler handler, ContentResolver contentResolver)
    {
        super(handler);
        this.contentResolver = contentResolver;
    }

    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);
        Uri uriSMSURI = Uri.parse("content://sms");
        Cursor cur = contentResolver.query(uriSMSURI, null, null, null, null);
        if (cur != null)
        {
            cur.moveToNext();
            String content = cur.getString(cur.getColumnIndex("body"));
            String smsNumber = cur.getString(cur.getColumnIndex("address"));
            int type = cur.getInt(cur.getColumnIndex("type"));
            //TODO: Implement more types. See URL below
            //http://stackoverflow.com/questions/15352103/android-documentation-for-content-sms-type-values

            if (type == 2)
            {
                //TODO: Send message to server
                Log.v("PostalMessenger", "CUR " + Arrays.toString(cur.getColumnNames()));
                Log.v("PostalMessenger", "SEND " + smsNumber + " " + content);
            }
        }
    }
}
