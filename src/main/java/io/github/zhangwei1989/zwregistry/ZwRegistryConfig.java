package io.github.zhangwei1989.zwregistry;

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

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
        return new ZwHealthChecker(registryService);
    }

}
