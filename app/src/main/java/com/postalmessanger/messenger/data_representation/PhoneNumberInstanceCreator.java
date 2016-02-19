package com.postalmessanger.messenger.data_representation;

import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;

/**
 * Created by kenny on 2/19/16.
 */
public class PhoneNumberInstanceCreator implements InstanceCreator<PhoneNumber> {
    public PhoneNumber createInstance(Type type) {
        return new PhoneNumber();
    }
}
