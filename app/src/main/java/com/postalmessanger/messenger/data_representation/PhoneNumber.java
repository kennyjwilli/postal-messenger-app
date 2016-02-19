package com.postalmessanger.messenger.data_representation;

import com.postalmessanger.messenger.util.Util;

/**
 * Created by kenny on 2/19/16.
 */
public class PhoneNumber {
    public String type;
    public String number;

    public PhoneNumber(String type, String phoneNumber) {
        this.type = type;
        this.number = phoneNumber;
    }

    public PhoneNumber(int type, String phoneNumber) {
        this(Util.getPhoneNumberTypeString(type), phoneNumber);
    }

    public PhoneNumber(String phoneNumber) {
        this(null, phoneNumber);
    }

    public PhoneNumber() {
    }

    public String toString() {
        return "{:type " + type + " :phoneNumber " + number + "}";
    }
}
