package io.github.zhangwei1989.zwregistry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 注册中心配置属性类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/21
 */
@Data
@ConfigurationProperties(prefix = "zwregistry")
public class ZwregistryConfigProperties {

    private List<String> serverUrls;

}
