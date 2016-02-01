package com.postalmessanger.messenger.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by kenny on 1/31/16.
 */
public class Http
{
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String BASE_URL = "http://1eee517b.ngrok.com";

    public static void post(String url, Map<String, String> headers, String json, Callback callback) throws IOException
    {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Request req = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(body)
                .build();
        client.newCall(req).enqueue(callback);
    }

    public static void post(String url, String json, Callback callback) throws IOException
    {
        post(url, new HashMap<String, String>(), json, callback);
    }

    public static void get(String url, Map<String, String> headers, Callback callback)
    {
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .build();
        client.newCall(req).enqueue(callback);
    }
}
