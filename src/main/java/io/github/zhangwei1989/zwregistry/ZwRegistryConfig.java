package io.github.zhangwei1989.zwregistry;

import io.github.zhangwei1989.zwregistry.cluster.Cluster;
import io.github.zhangwei1989.zwregistry.config.ZwregistryConfigProperties;
import io.github.zhangwei1989.zwregistry.health.HealthChecker;
import io.github.zhangwei1989.zwregistry.health.ZwHealthChecker;
import io.github.zhangwei1989.zwregistry.service.RegistryService;
import io.github.zhangwei1989.zwregistry.service.ZwRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * configuration for all beans
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/14
 */
@Configuration
public class ZwRegistryConfig {

    @Bean
    public RegistryService registryService() {
        return new ZwRegistryService();
    }

    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired ZwregistryConfigProperties registryConfigProperties) {
        return new Cluster(registryConfigProperties);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
        return new ZwHealthChecker(registryService);
    }

}
