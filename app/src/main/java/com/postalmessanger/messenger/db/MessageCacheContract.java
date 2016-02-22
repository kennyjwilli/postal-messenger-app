package com.postalmessanger.messenger.db;

import android.provider.BaseColumns;

/**
 * Created by kenny on 2/20/16.
 */
public class MessageCacheContract {
    public MessageCacheContract() {
    }

    public static abstract class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COL_ID = "id";
    }
}
