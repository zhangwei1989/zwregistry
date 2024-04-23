package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.client.http.HttpInvoker;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * cluster server health check
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/23
 */
@Slf4j
public class ServerHealth {

    private Cluster cluster;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    public void checkHealth() {
        try {
            // 启动定时任务
            executorService.scheduleWithFixedDelay(() -> {
                // 定时探活所有注册中心节点
                updateServers();
                // 定时选主
                electLeader();
                // 同步主节点注册数据
                syncFromLeader();
            }, 5, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateServers() {
        // 请求每个实例的 /health 接口，更新实例的状态
        cluster.getServers().forEach(server -> {
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
        Election.electLeader(cluster.getServers());
    }

    private void syncFromLeader() {
        Server self = cluster.self();
        Server leader = cluster.leader();

        if (!self.isLeader() && self.getVersion() < leader.getVersion()) {
            log.debug(" ======> leader version is {}, current server version is {}",
                    leader.getVersion(), self.getVersion());
            log.debug(" ======> sync snapshot from leader start......");
            Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
            cluster.getRegistryService().restore(snapshot);
            log.debug(" ======> sync snapshot from leader success");
        }
    }
}
