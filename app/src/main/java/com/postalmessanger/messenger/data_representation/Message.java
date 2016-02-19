package com.postalmessanger.messenger.data_representation;

import java.util.List;

/**
 * Created by kenny on 2/18/16.
 */
//{:type received, :recipients [6505551212], :timestamp 2016-02-18T18:03:40.000, :data Don't forget the marshmallows!}}
public class Message
{
    public String type;
    public List<String> recipients;
    public String timestamp;
    public String data;
}
