package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * 注册信息快照
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/22 22:56
 */
@Data
@AllArgsConstructor
public class Snapshot {

    private LinkedMultiValueMap<String, InstanceMeta> REGISTRY;

    private Map<String, Long> VERSIONS;

    private Map<String, Long> TIMESTAMPS;

    private long VERSION;

}
