package com.postalmessanger.messenger.data_representation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by kenny on 2/18/16.
 */
public class Json
{
    public static Event fromJson(String json)
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Message.class, new MessageInstanceCreator());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(json, Event.class);
    }

    public static String toJson(Event evt)
    {
        Gson gson = new Gson();
        return gson.toJson(evt);
    }
}
