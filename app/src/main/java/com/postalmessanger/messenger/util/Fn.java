package com.postalmessanger.messenger.util;

/**
 * Created by kenny on 2/18/16.
 */
public abstract class Fn {
    public void onSuccess(Object... args) {}

    public void onSuccess() {
        onSuccess(new Object[]{});
    }

    public abstract void onError();
}
