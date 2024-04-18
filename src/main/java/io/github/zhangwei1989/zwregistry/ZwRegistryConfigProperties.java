package io.github.zhangwei1989.zwregistry;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * registry config properties.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/17
 */
@Data
@ConfigurationProperties(prefix = "zwregistry")
public class ZwRegistryConfigProperties {

    private List<String> serverList;
}
