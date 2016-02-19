package com.postalmessanger.messenger.data_representation;

import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;

/**
 * Created by kenny on 2/18/16.
 */
public class MessageInstanceCreator implements InstanceCreator<Message>
{
    public Message createInstance(Type type)
    {
        return new Message();
    }
}
