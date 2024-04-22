package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.http.HttpInvoker;
import io.github.zhangwei1989.zwregistry.service.ZwRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * check health for servers.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/22
 */
@Slf4j
public class ServerHealth {

    final Cluster cluster;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    long timeout = 5_000;

    public void checkServerHealth() {
        executorService.scheduleWithFixedDelay(() -> {
            try {
                updateServers();                // update servers
                doElect();                      // elect leader
                syncSnapshotFromLeader();       // sync snapshot from leader
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    private void doElect() {
        new Election().electLeader(cluster.getServers());
    }

    private void updateServers() {
        cluster.getServers().stream().parallel().forEach(server -> {
            try {
                if (server.equals(cluster.self())) {
                    return;
                }
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

    private void syncSnapshotFromLeader() {
        Server self = cluster.self();
        Server leader = cluster.leader();
        if (!self.isLeader() && self.getVersion() < leader.getVersion()) {
            log.debug(" ======> leader version : {}, my version : {}", leader.getVersion(), cluster.self().getVersion());
            log.debug(" ======> sync snapshot from leader : {}", leader);
            Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
            ZwRegistryService.restore(snapshot);
            log.debug(" ======> sync snapshot from leader success");
        }
    }

}
