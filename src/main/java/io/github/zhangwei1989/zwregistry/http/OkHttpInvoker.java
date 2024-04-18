package io.github.zhangwei1989.zwregistry.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

/**
 * OkHttpInvoker.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    OkHttpClient client;

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    public OkHttpInvoker(int timeout) {
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Override
    public String post(String requestString, String url) {
        log.debug(" ======> post url = {}", url);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, JSONTYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug("===> respJSON: " + respJson);
            return respJson;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {
        log.debug(" ======> get url = {}", url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug("===> respJSON: " + respJson);
            return respJson;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
