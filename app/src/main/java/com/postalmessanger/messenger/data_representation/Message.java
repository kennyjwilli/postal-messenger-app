package com.postalmessanger.messenger.data_representation;

import com.postalmessanger.messenger.util.Util;

import java.util.List;

/**
 * Created by kenny on 2/18/16.
 */
public class Message {
    public String type;
    public List<String> recipients;
    public long timestamp;
    public String date;
    public String text;
    public int idx;

    public Message(String type, List<String> recipients, long timestamp, String text, int idx) {
        this.type = type;
        this.recipients = recipients;
        this.timestamp = timestamp;
        this.date = Util.formatTimestamp(timestamp);
        this.text = text;
        this.idx = idx;
    }

    public Message(String type, List<String> recipients, long timestamp, String text) {
        this(type, recipients, timestamp, text, -1);
    }

    public Message() {}
}
