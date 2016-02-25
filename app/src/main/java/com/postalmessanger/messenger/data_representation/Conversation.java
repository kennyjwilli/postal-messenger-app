package com.postalmessanger.messenger.data_representation;

import java.util.List;

/**
 * Created by kenny on 2/25/16.
 */
public class Conversation {
    public String thread_id;
    public List<String> recipients;
    public List<Message> messages;

    public Conversation(String thread_id, List<String> recipients, List<Message> messages) {
        this.thread_id = thread_id;
        this.recipients = recipients;
        this.messages = messages;
    }
}
