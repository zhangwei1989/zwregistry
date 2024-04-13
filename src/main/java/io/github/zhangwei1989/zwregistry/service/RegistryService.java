package io.github.zhangwei1989.zwregistry.service;

import io.github.zhangwei1989.zwregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * Interface for registry service
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/14
 */
public interface RegistryService {

    // 最基础的 3 个方法
    InstanceMeta register(String service, InstanceMeta instance);

    InstanceMeta unregister(String service, InstanceMeta instance);

    List<InstanceMeta> getAllInstances(String serviceName);

    // TODO 添加一些高级功能
    long renew(InstanceMeta instance, String... services);

    Long version(String service);

    Map<String, Long> versions(String... services);

}
