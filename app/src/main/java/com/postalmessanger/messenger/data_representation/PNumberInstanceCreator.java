package com.postalmessanger.messenger.data_representation;

import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;

/**
 * Created by kenny on 2/19/16.
 */
public class PNumberInstanceCreator implements InstanceCreator<PNumber> {
    public PNumber createInstance(Type type) {
        return new PNumber();
    }
}
