package io.github.zhangwei1989.zwregistry.health;

import io.github.zhangwei1989.zwregistry.model.InstanceMeta;
import io.github.zhangwei1989.zwregistry.service.RegistryService;
import io.github.zhangwei1989.zwregistry.service.ZwRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/14
 */
@Slf4j
public class ZwHealthChecker implements HealthChecker {

    RegistryService registryService;

    public ZwHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    long timeout = 20_000;

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(() -> {
            log.info(" ======> Health checker running...");
            long now = System.currentTimeMillis();
            ZwRegistryService.TIMESTAMPS.keySet().forEach(serviceAndInstance -> {
                long last = ZwRegistryService.TIMESTAMPS.get(serviceAndInstance);
                if (now - last > timeout) {
                    log.info(" ======> Service {} is not available", serviceAndInstance);
                    int index = serviceAndInstance.indexOf("@");
                    String service = serviceAndInstance.substring(0, index);
                    String url = serviceAndInstance.substring(index + 1);
                    InstanceMeta instance = InstanceMeta.fromUrl(url);
                    registryService.unregister(service, instance);
                    ZwRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                }
            });
        }, 10, 30, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executor.shutdown();
    }

}
