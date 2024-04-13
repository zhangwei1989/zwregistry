package io.github.zhangwei1989.zwregistry.service;

import io.github.zhangwei1989.zwregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * RegistryService的默认实现
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/14
 */
@Slf4j
public class ZwRegistryService implements RegistryService {

    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();

    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();

    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();

    final static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> instanceMetas = REGISTRY.get(service);
        if (instanceMetas != null && !instanceMetas.isEmpty()) {
            if (instanceMetas.contains(instance)) {
                log.info(" ======> instance {} already registered", instance.toUrl());
                instance.setStatus(true);
                return instance;
            }
        }

        log.info(" ======> register instance {}, ", instance.toUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> instanceMetas = REGISTRY.get(service);
        if (instanceMetas == null || instanceMetas.isEmpty()) {
            return null;
        }

        log.info(" ======> unregister instance {} ", instance.toUrl());
        instanceMetas.removeIf(instanceMeta -> instanceMeta.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String serviceName) {
        return REGISTRY.get(serviceName);
    }

    public long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }

        return now;
    }

    public Long version(String service) {
        return VERSIONS.get(service);
    }

    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services).collect(Collectors.toMap(x -> x, x -> VERSIONS.get(x), (a, b) -> b));
    }
}
