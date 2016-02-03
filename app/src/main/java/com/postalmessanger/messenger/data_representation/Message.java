package com.postalmessanger.messenger.data_representation;

import java.util.Date;

/**
 * Created by kenny on 2/1/16.
 */
public class Message
{
    private String type;
    private long timestamp;
    private String sender;
    private String data;

    public Message(String type, long timestamp, String sender, String data)
    {
        this.type = type;
        this.timestamp = timestamp;
        this.sender = sender;
        this.data = data;
    }
}
