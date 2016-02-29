package com.postalmessanger.messenger.data_representation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Created by kenny on 2/18/16.
 */
public class Json {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Contact.class, new Serializer.ContactSerializer())
            .registerTypeAdapter(Conversation.class, new Serializer.ConversationSerializer())
            .registerTypeAdapter(ConversationSnippet.class, new Serializer.ConversationSnippetSerializer())
            .registerTypeAdapter(Event.class, new Serializer.EventSerializer())
            .registerTypeAdapter(Message.class, new Serializer.MessageSerializer())
            .registerTypeAdapter(PhoneNumber.class, new Serializer.PhoneNumberSerializer())
            .create();

    public static Event fromJson(String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.registerTypeAdapter(Message.class, new MessageInstanceCreator());
        gsonBuilder.registerTypeAdapter(Contact.class, new ContactInstanceCreator());
        gsonBuilder.registerTypeAdapter(PhoneNumber.class, new PhoneNumberInstanceCreator());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(json, Event.class);
    }

    public static JsonElement toJson(Object src) {
        return gson.toJsonTree(src);
    }
}
