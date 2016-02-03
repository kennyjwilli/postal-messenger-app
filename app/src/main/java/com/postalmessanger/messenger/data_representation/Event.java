package com.postalmessanger.messenger.data_representation;

import com.postalmessanger.messenger.util.Util;

/**
 * Created by kenny on 2/1/16.
 */
public class Event
{
    private String socket_id;
    private String dest;
    private String type;
    private Message message;

    public Event(String socket_id, String dest, String type, Message msg)
    {
        this.socket_id = socket_id;
        this.dest = dest;
        this.type = type;
        this.message = msg;
    }

    public Event(String dest, String type, Message msg)
    {
        this(Util.socket_id, dest, type, msg);
    }
}
