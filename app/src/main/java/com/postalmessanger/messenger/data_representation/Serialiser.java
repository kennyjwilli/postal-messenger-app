package com.postalmessanger.messenger.data_representation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by kenny on 2/25/16.
 */
public class Serialiser {

    public static class ContactSerialiser implements JsonSerializer<Contact> {
        @Override
        public JsonElement serialize(Contact src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("i", src.id);
            obj.addProperty("n", src.name);
            Gson gson = new GsonBuilder().registerTypeAdapter(PhoneNumber.class, new PhoneNumberSerialiser()).create();
            obj.add("p", gson.toJsonTree(src.phoneNumbers));
            return obj;
        }
    }

    public static class ConversationSerialiser implements JsonSerializer<Conversation> {

        @Override
        public JsonElement serialize(Conversation src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageSerialiser()).create();
            json.addProperty("t", src.thread_id);
            json.add("r", new Gson().toJsonTree(src.recipients));
            json.add("m", gson.toJsonTree(src.messages));
            return json;
        }
    }

    public static class ConversationSnippetSerialiser implements JsonSerializer<ConversationSnippet> {

        @Override
        public JsonElement serialize(ConversationSnippet src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("th", src.thread_id);
            json.addProperty("d", src.date);
            json.addProperty("a", src.address);
            json.addProperty("b", src.text);
            return json;
        }
    }

    public static class EventSerialiser implements JsonSerializer<Event> {
        @Override
        public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageSerialiser()).create();
            json.addProperty("d", src.dest);
            json.addProperty("t", src.type);
            json.addProperty("s", src.socket_id);
            json.add("m", gson.toJsonTree(src.data));
            return json;
        }
    }

    public static class MessageSerialiser implements JsonSerializer<Message> {
        @Override
        public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("t", src.type);
            json.add("r", new Gson().toJsonTree(src.recipients));
            json.addProperty("d", src.date);
            json.addProperty("b", src.text);
            if (src.idx >= 0) { json.addProperty("i", src.idx);}
            return json;
        }
    }

    public static class PhoneNumberSerialiser implements JsonSerializer<PhoneNumber> {
        @Override
        public JsonElement serialize(PhoneNumber src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("t", src.type);
            obj.addProperty("n", src.number);
            return obj;
        }
    }

}
