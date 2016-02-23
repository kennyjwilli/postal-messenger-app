package com.postalmessanger.messenger.data_representation;

/**
 * Created by kenny on 2/18/16.
 */
//{:dest client, :type add-message, :socket_id 177824.422016, :message {:type received, :recipients [6505551212], :timestamp 2016-02-18T18:03:40.000, :text Don't forget the marshmallows!}}
public class Event {
    public String dest;
    public String type;
    public String socket_id;
    public Message data;
}
