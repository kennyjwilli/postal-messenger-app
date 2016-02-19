package com.postalmessanger.messenger.data_representation;

import com.google.gson.JsonElement;

import java.util.List;

/**
 * Created by kenny on 2/18/16.
 */
public class Message {
    public String type;
    public List<Contact> recipients;
    public String timestamp;
    public String data;
}
