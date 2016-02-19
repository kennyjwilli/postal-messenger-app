package com.postalmessanger.messenger.data_representation;

import java.util.List;

/**
 * Created by kenny on 2/18/16.
 */
public class Contact {
    public String id;
    public String name;
    public List<String> phoneNumbers;

    public Contact(String id, String name, List<String> phoneNumbers) {
        this.id = id;
        this.name = name;
        this.phoneNumbers = phoneNumbers;
    }

    public Contact() {
    }

    public String toString()
    {
        return "{:id " + id + " :name " + name + " :phoneNumbers " + phoneNumbers;
    }
}
