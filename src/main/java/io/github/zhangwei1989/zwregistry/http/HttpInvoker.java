package io.github.zhangwei1989.zwregistry.http;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HttpInvoker interface.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker Default = new OkHttpInvoker(500);

    String post(String requestString, String url);

    String get(String url);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" ======> httpGet url = {}", url);
        String respJson = Default.get(url);
        log.debug(" ======> response = {}", respJson);
        return JSON.parseObject(respJson, clazz);
    }

}
