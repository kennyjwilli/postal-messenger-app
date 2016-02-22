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

    public static void insertMessage(SQLiteDatabase db, int id) {
        String sql = "INSERT OR IGNORE INTO " + MessageEntry.TABLE_NAME +
                " (" + MessageEntry.COL_ID + ") " +
                "VALUES (" + id + ") ";
        db.execSQL(sql);
    }

    public static void insertMessage(SQLiteDatabase db, String uri) {
        insertMessage(db, Util.getMessageNumber(uri));
    }

    public static boolean hasMessage(SQLiteDatabase db, int id) {
        String sql = "SELECT * FROM " + MessageEntry.TABLE_NAME +
                " WHERE " + MessageEntry.COL_ID + " = " + id + "";
        Cursor cur = db.rawQuery(sql, null);
        if (cur != null) {
            //Log.v("PostalMessenger", DatabaseUtils.dumpCursorToString(cur));
            if (cur.moveToFirst() && cur.getCount() > 0) {
                long l = cur.getLong(cur.getColumnIndex(MessageEntry.COL_ID));
                if (l == id) {
                    return true;
                }
            }
            cur.close();
        }
        return false;
    }

    public static boolean hasMessage(SQLiteDatabase db, String uri) {
        return hasMessage(db, Util.getMessageNumber(uri));
    }
}
