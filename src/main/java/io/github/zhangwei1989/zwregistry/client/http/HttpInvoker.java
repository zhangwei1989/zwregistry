package io.github.zhangwei1989.zwregistry.client.http;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpInvoker
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker DEFAULT = new OkHttpInvoker(3000);

    public String post(String requestBody, String url);

    public String get(String url);

    static Object httpGet(String url, Class<?> clazz) {
        log.debug("httpGet, url ======> {}", url);
        String respJson = DEFAULT.get(url);
        log.debug("httpGet, url, respJson ======> {}, {}", url, respJson);
        return JSON.parseObject(respJson, clazz);
    }

    static Object httpPost(String requestBody, String url, Class<?> clazz) {
        log.debug("httpPost, url, requestBody ======> {}, {}", url, requestBody);
        String respJson = DEFAULT.post(requestBody, url);
        log.debug("httpPost, url, requestBody, respJson ======> {}, {}, {}", url, requestBody, respJson);
        return JSON.parseObject(respJson, clazz);
    }

}
