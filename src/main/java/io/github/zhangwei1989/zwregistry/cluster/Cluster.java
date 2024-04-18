package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.ZwRegistryConfigProperties;
import io.github.zhangwei1989.zwregistry.http.HttpInvoker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Registry cluster.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/16
 */
@Slf4j
public class Cluster {

    ZwRegistryConfigProperties registryConfigProperties;

    String host;

    @Value("${server.port}")
    String port;

    Server MYSELF;

    @Getter
    List<Server> servers;

    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    long timeout = 5_000;

    public Cluster(ZwRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    public void init() {
        host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
        log.debug(" ======> findFirstNonLoopbackHostInfo host : {}", host);
        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.debug(" ======> MYSELF : {}", MYSELF);

        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            Server server = new Server();
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }

            if (url.equals(MYSELF.getUrl())) {
                servers.add(MYSELF);
            } else {
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1L);
                servers.add(server);
            }
        }

        this.servers = servers;

        executorService.scheduleWithFixedDelay(() -> {
            updateServers();
            electLeader();
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    private void updateServers() {
        servers.forEach(server -> {
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.debug(" ======> health check success for : {}", serverInfo);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                } else {
                    server.setStatus(false);
                    server.setLeader(false);
                }
            } catch (Exception exception) {
                log.debug(" ======> health check failed for : {}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    private void electLeader() {
        List<Server> masters = this.servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .collect(Collectors.toList());
        if (masters.isEmpty()) {
            log.debug(" ======> no master, need to elect : {}", servers);
            elect();
        } else if (masters.size() > 1) {
            log.debug(" ======> more than one master, need to re-elect : {}", servers);
            elect();
        } else {
            log.debug(" ======> no need election for leader : {}", masters.get(0));
        }
    }

    private void elect() {
        // 1. 各个节点自己选，算法保证大家选的是同一个
        // 2. 外部有一个分布式锁，谁拿到锁，谁是主
        // 3. 分布式一致性算法，比如paxos,raft...
        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    if (server.hashCode() < candidate.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }

        if (candidate != null) {
            candidate.setLeader(true);
            log.debug(" ======> elected leader : {}", candidate);
        } else {
            log.debug(" ======> no leader elected");
        }
    }

    public Server self() {
        return MYSELF;
    }

    public Server leader() {
        return servers.stream().filter(Server::isLeader).findFirst().orElse(null);
    }

}
