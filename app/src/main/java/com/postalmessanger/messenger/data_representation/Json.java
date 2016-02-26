package com.postalmessanger.messenger.data_representation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Created by kenny on 2/18/16.
 */
public class Json {
    public static Event fromJson(String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.registerTypeAdapter(Message.class, new MessageInstanceCreator());
        gsonBuilder.registerTypeAdapter(Contact.class, new ContactInstanceCreator());
        gsonBuilder.registerTypeAdapter(PhoneNumber.class, new PhoneNumberInstanceCreator());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(json, Event.class);
    }

    public static Gson toGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Contact.class, new Serialiser.ContactSerialiser())
                .registerTypeAdapter(Conversation.class, new Serialiser.ConversationSerialiser())
                .registerTypeAdapter(ConversationSnippet.class, new Serialiser.ConversationSnippetSerialiser())
                .registerTypeAdapter(Event.class, new Serialiser.EventSerialiser())
                .registerTypeAdapter(Message.class, new Serialiser.MessageSerialiser())
                .registerTypeAdapter(PhoneNumber.class, new Serialiser.PhoneNumberSerialiser())
                .create();
    }

    public static JsonElement toJson(Object src){
        return toGson().toJsonTree(src);
    }
}
