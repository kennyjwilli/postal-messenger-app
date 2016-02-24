package com.postalmessanger.messenger.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by kenny on 2/1/16.
 * SLAPI = Saving Loading API
 */
public class SLAPI {
    public static final String settingsPrefName = "settings";

    public static final String API_KEY = "api-key";
    public static final String MESSAGE_CHANNEL = "message-channel";

    public static void saveValues(Context ctx, String loc, Map<String, String> kv) {
        SharedPreferences prefs = ctx.getSharedPreferences(loc, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (String s : kv.keySet()) {
            editor.putString(s, kv.get(s));
        }
        editor.apply();
    }

    public static void saveValues(Context ctx, Map<String, String> kv) {
        saveValues(ctx, settingsPrefName, kv);
    }

    public static void saveValue(Context ctx, String loc, String k, String v) {
        SharedPreferences prefs = ctx.getSharedPreferences(loc, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(k, v);
        editor.apply();
    }

    public static void saveValue(Context ctx, String k, String v) {
        saveValue(ctx, settingsPrefName, k, v);
    }

    public static String getSavedValue(Context ctx, String loc, String k) {
        SharedPreferences prefs = ctx.getSharedPreferences(loc, Context.MODE_PRIVATE);
        return prefs.getString(k, null);
    }

    public static String getSavedValue(Context ctx, String k) {
        return getSavedValue(ctx, settingsPrefName, k);
    }

    public static String getToken(Context ctx) {
        return getSavedValue(ctx, "Token");
    }

    public static boolean hasToken(Activity a) {
        return getToken(a) != null;
    }

    public static void saveToken(Context ctx, String token) {
        saveValue(ctx, "Token", token);
    }

    public static void deleteToken(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(settingsPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("Token");
        editor.apply();
    }
}
