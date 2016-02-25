package com.postalmessanger.messenger.data_representation;

import com.postalmessanger.messenger.util.Util;

/**
 * Created by kenny on 2/24/16.
 */
public class ConversationSnippet {
    public String thread_id;
    public long timestamp;
    public String date;
    public String address;
    public String text;

    public ConversationSnippet(String thread_id, long timestamp, String address, String text) {
        this.thread_id = thread_id;
        this.timestamp = timestamp;
        this.date = Util.formatTimestamp(timestamp);
        this.address = Util.normalizePhoneNumber(address);
        this.text = text;
    }
}
