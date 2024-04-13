package io.github.zhangwei1989.zwregistry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * InstanceMeta model.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"schema", "host", "port", "context"})
public class InstanceMeta {

    private String schema;

    private String host;

    private String port;

    private String context;

    private boolean status; // online or offline

    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String schema, String host, String port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public static InstanceMeta fromUrl(String url) {
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(),
                uri.getHost(),
                String.valueOf(uri.getPort()),
                uri.getPath().substring(1));

    }

    public String toPath() {
        return String.format("%s_%s", host, port);
    }

    public static InstanceMeta http(String host, String port) {
        return new InstanceMeta("http", host, port, "wmrpc");
    }

    public String toUrl() {
        return String.format("%s://%s:%s/%s", schema, host, port, context);
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }

    public InstanceMeta addParams(Map<String, String> map) {
        this.parameters.putAll(map);
        return this;
    }
}
