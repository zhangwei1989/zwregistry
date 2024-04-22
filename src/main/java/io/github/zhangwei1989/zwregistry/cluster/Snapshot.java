package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    LinkedMultiValueMap<String, InstanceMeta> REGISTRY;

    Map<String, Long> VERSIONS;

    Map<String, Long> TIMESTAMPS;

    Long VERSION;

}
