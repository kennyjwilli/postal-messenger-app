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
public class Serializer {

    public static class ContactSerializer implements JsonSerializer<Contact> {
        @Override
        public JsonElement serialize(Contact src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("n", src.name);
            Gson gson = new GsonBuilder().registerTypeAdapter(PhoneNumber.class, new PhoneNumberSerializer()).create();
            obj.add("p", gson.toJsonTree(src.phoneNumbers));
            return obj;
        }
    }

    public static class ConversationSerializer implements JsonSerializer<Conversation> {

        @Override
        public JsonElement serialize(Conversation src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageSerializer()).create();
            json.addProperty("t", src.thread_id);
            if (src.recipients != null) {
                json.add("r", new Gson().toJsonTree(src.recipients));
            }
            if (src.messages != null) {
                json.add("m", gson.toJsonTree(src.messages));
            }
            return json;
        }
    }

    public static class ConversationSnippetSerializer implements JsonSerializer<ConversationSnippet> {

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

    public static class EventSerializer implements JsonSerializer<Event> {
        @Override
        public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageSerializer()).create();
            json.addProperty("d", src.dest);
            json.addProperty("t", src.type);
            json.addProperty("s", src.socket_id);
            json.add("m", gson.toJsonTree(src.data));
            return json;
        }
    }

    public static class MessageSerializer implements JsonSerializer<Message> {
        @Override
        public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("t", src.type);
            if (src.recipients != null) {
                json.add("r", new Gson().toJsonTree(src.recipients));
            }
            json.addProperty("d", src.date);
            json.addProperty("b", src.text);
            if (src.idx >= 0) { json.addProperty("i", src.idx);}
            return json;
        }
    }

    public static class PhoneNumberSerializer implements JsonSerializer<PhoneNumber> {
        @Override
        public JsonElement serialize(PhoneNumber src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("t", src.type);
            obj.addProperty("n", src.number);
            return obj;
        }
    }

}
