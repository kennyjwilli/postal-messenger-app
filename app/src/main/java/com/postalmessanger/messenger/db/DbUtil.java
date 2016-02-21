package com.postalmessanger.messenger.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.postalmessanger.messenger.util.Util;

import static com.postalmessanger.messenger.db.MessageCacheContract.MessageEntry;

/**
 * Created by kenny on 2/20/16.
 */
public class DbUtil {
    //Make sure to call this in a background thread, such as AsyncTask or IntentService
    public static SQLiteDatabase getWriteableDb(Context ctx) {
        return new MessageCacheDbHelper(ctx).getWritableDatabase();
    }

    public static SQLiteDatabase getReadableDb(Context ctx) {
        return new MessageCacheDbHelper(ctx).getReadableDatabase();
    }

    public static void insertMessage(SQLiteDatabase db, String uri) {
//        ContentValues values = new ContentValues();
//        values.put(MessageEntry.COL_URI, uri);
//        values.put(MessageEntry.COL_ID, Util.getMessageNumber(uri));
//        db.insert(MessageEntry.TABLE_NAME, null, values);

        String sql = "INSERT OR IGNORE INTO " + MessageEntry.TABLE_NAME +
                " (" + MessageEntry.COL_ID + "," + MessageEntry.COL_URI + ") " +
                "VALUES (" + Util.getMessageNumber(uri) + ",'" + uri + "') ";
        Log.v("PostalMessenger", sql);
        db.execSQL(sql);
    }

    public static boolean hasMessage(SQLiteDatabase db, String uri) {
        long msgNum = Util.getMessageNumber(uri);
        String sql = "SELECT * FROM " + MessageEntry.TABLE_NAME +
                " WHERE " + MessageEntry.COL_ID + " = '" + msgNum + "'";
        Cursor cur = db.rawQuery(sql, null);
        if (cur != null) {
            cur.moveToFirst();
            Log.v("PostalMessenger", DatabaseUtils.dumpCursorToString(cur));
            long l = cur.getLong(cur.getColumnIndex(MessageEntry.COL_ID));
            Log.v("PostalMessenger", "Q " + l);
            if (l == msgNum) {
                return true;
            }
            cur.close();
        }
        return false;
    }
}
