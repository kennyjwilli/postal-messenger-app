package com.postalmessanger.messenger.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

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

    public static void insertMessage(Context ctx, int id) {
        SQLiteDatabase db = getWriteableDb(ctx);
        String sql = "INSERT OR IGNORE INTO " + MessageEntry.TABLE_NAME +
                " (" + MessageEntry.COL_ID + ") " +
                "VALUES (" + id + ") ";
        db.execSQL(sql);
        db.close();
    }

    public static void insertMessage(Context ctx, String uri) {
        insertMessage(ctx, Util.getMessageNumber(uri));
    }

    public static void removeMessage(Context ctx, int id) {
        SQLiteDatabase db = getWriteableDb(ctx);
        db.delete(MessageEntry.TABLE_NAME, MessageEntry.COL_ID + "=" + id, null);
        db.close();
    }

    public static boolean hasMessage(Context ctx, int id) {
        SQLiteDatabase db = getReadableDb(ctx);
        String sql = "SELECT * FROM " + MessageEntry.TABLE_NAME +
                " WHERE " + MessageEntry.COL_ID + " = " + id + "";
        Cursor cur = db.rawQuery(sql, null);
        boolean result = false;
        if (cur != null) {
            //Log.v("PostalMessenger", DatabaseUtils.dumpCursorToString(cur));
            if (cur.moveToFirst() && cur.getCount() > 0) {
                long l = cur.getLong(cur.getColumnIndex(MessageEntry.COL_ID));
                if (l == id) {
                    result = true;
                }
            }
            cur.close();
        }
        db.close();
        return result;
    }

    public static boolean hasMessage(Context ctx, String uri) {
        return hasMessage(ctx, Util.getMessageNumber(uri));
    }

    public static int getCurrentSmsId(Context ctx) {
        Uri uri = Uri.parse("content://sms/");
        ContentResolver cr = ctx.getContentResolver();
        Cursor c = cr.query(uri, new String[]{"_id"}, null, null, null);
        if (c != null) {
            c.moveToFirst();
            int id = c.getInt(c.getColumnIndex("_id"));
            c.close();
            return id;
        }
        return -2;
    }

    public static int getNextSmsId(Context ctx) {
        return getCurrentSmsId(ctx) + 1;
    }
}
