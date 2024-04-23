package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.config.ZwregistryConfigProperties;
import io.github.zhangwei1989.zwregistry.service.RegistryService;
import io.github.zhangwei1989.zwregistry.service.ZwRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 注册中心集群
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/21
 */
@Slf4j
public class Cluster {

    @Autowired
    @Getter
    private RegistryService registryService;

    ZwregistryConfigProperties registryConfigProperties;

    @Value("${server.port}")
    private String port;

    private Server MYSELF;

    @Getter
    private List<Server> servers;

    public Cluster(ZwregistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    // 集群类初始化方法
    // 启动定时任务，定时探活配置中的注册中心所有实例
    // 定时确认是否需要选主，以及必要时进行选主
    public void init() {
        initServers();
        new ServerHealth(this).checkHealth();
    }

    private void initServers() {
        // 首先初始化 MYSELF
        String host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);

        // 初始化 servers
        servers = new ArrayList<>();
        registryConfigProperties.getServerUrls().forEach(serverUrl -> {
            if (serverUrl.contains("127.0.0.1") && !("127.0.0.1".equals(host))) {
                serverUrl = serverUrl.replace("127.0.0.1", host);
            }
            Server server = new Server(serverUrl, false, false, -1L);
            if (MYSELF.equals(server)) {
                servers.add(MYSELF);
            } else {
                servers.add(server);
            }
        });
    }

    public Server self() {
        MYSELF.setVersion(ZwRegistryService.VERSION.get());
        return MYSELF;
    }

    public Server leader() {
        return this.getServers().stream()
                .filter(Server::isLeader)
                .findFirst()
                .orElse(null);
    }

}
