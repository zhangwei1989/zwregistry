package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.client.http.HttpInvoker;
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
import java.util.concurrent.TimeUnit;

/**
 * 注册中心集群
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/21
 */
@Slf4j
public class Cluster {

    @Autowired
    private RegistryService registryService;

    ZwregistryConfigProperties registryConfigProperties;

    @Value("${server.port}")
    private String port;

    private Server MYSELF;

    @Getter
    private List<Server> servers;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public Cluster(ZwregistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    // 集群类初始化方法
    // 启动定时任务，定时探活配置中的注册中心所有实例
    // 定时确认是否需要选主，以及必要时进行选主
    public void init() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // 启动定时任务
            executorService.scheduleWithFixedDelay(() -> {
                // 定时探活所有注册中心节点
                updateServers();
                // 定时选主
                electLeader();
                // 同步主节点注册数据
                syncFromLeader();
            }, 0, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncFromLeader() {
        if (!self().isLeader() && self().getVersion() < leader().getVersion()) {
            log.debug(" ======> leader version is {}, current server version is {}",
                    leader().getVersion(), self().getVersion());
            log.debug(" ======> sync snapshot from leader start......");
            Snapshot snapshot = HttpInvoker.httpGet(leader().getUrl() + "/snapshot", Snapshot.class);
            registryService.restore(snapshot);
            log.debug(" ======> sync snapshot from leader success");
        }
    }

    private void updateServers() {
        // 请求每个实例的 /health 接口，更新实例的状态
        servers.forEach(server -> {
            try {
                Server instance = HttpInvoker.httpGet(server.getUrl() + "/health", Server.class);
                if (instance != null) {
                    server.setStatus(true);
                    server.setLeader(instance.isLeader());
                    server.setVersion(instance.getVersion());
                } else {
                    server.setStatus(false);
                    server.setLeader(false);
                }
            } catch (Exception e) {
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    private void electLeader() {
        // 所有节点执行相同的选主逻辑，选出来的都是同一个主
        List<Server> masters = servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .toList();
        // 如果主节点大于 1 个，需要重新选主
        if (masters.size() > 1) {
            log.debug("more than one leader server, need to re-elect: {}", masters);
            doElect();
        } else if (masters.size() == 0) { // 如果没有主节点，也需要选主
            log.debug("no leader server, need to elect: {}", masters);
            doElect();
        } else { // 其余情况不需要选主
            log.debug("no  need for elect, current leader is : {}", masters);
        }
    }

    private void doElect() {
        List<Server> candidates = servers.stream()
                .filter(Server::isStatus)
                .toList();
        Server candidate = null;
        for (Server server : candidates) {
            server.setLeader(false);
            if (candidate == null) {
                candidate = server;
                continue;
            }

            if (candidate.hashCode() > server.hashCode()) {
                candidate = server;
            }
        }

        if (candidate != null) {
            log.debug("elected leader is {}", candidate);
            candidate.setLeader(true);
        } else {
            log.debug("candidate is null, choose self as leader");
            self().setLeader(true);
        }
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
