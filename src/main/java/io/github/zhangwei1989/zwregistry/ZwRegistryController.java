package io.github.zhangwei1989.zwregistry;

import io.github.zhangwei1989.zwregistry.cluster.Cluster;
import io.github.zhangwei1989.zwregistry.cluster.Server;
import io.github.zhangwei1989.zwregistry.cluster.Snapshot;
import io.github.zhangwei1989.zwregistry.model.InstanceMeta;
import io.github.zhangwei1989.zwregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Rest controller for registry service
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/14
 */
@Slf4j
@RestController
public class ZwRegistryController {

    @Autowired
    RegistryService registryService;

    @Autowired
    Cluster cluster;

    @RequestMapping("/reg")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ======> register service {} with instance {}", service, instance);
        return registryService.register(service, instance);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unreg(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ======> unregister service {} with instance {}", service, instance);
        return registryService.unregister(service, instance);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam String service) {
        log.info(" ======> findAllInstances for service {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("/renew")
    public long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ======> renew service {} with instance {}", service, instance);
        return registryService.renew(instance, service);
    }

    @RequestMapping("/renews")
    public long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
        log.info(" ======> renew service {} with instance {}", services, instance);
        return registryService.renew(instance, services.split(","));
    }

    @RequestMapping("/version")
    public Long version(@RequestParam String service) {
        log.info(" ======> version for service {}", service);
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String services) {
        log.info(" ======> versions for services {}", services);
        return registryService.versions(services.split(","));
    }

    @RequestMapping("/health")
    public Server health() {
        log.info(" ======> health check, {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ======> cluster check, {}", cluster.getServers());
        return cluster.getServers();
    }

    @RequestMapping("/leader")
    public Server leader() {
        log.info(" ======> leader, {}", cluster.leader());
        return cluster.leader();
    }

    @RequestMapping("/snapshot")
    public Snapshot snapshot() {
        log.info(" ======> current server snapshot, {}", registryService.snapshot());
        return registryService.snapshot();
    }

}
