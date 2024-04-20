package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.client.http.HttpInvoker;
import io.github.zhangwei1989.zwregistry.config.ZwregistryConfigProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

/**
 * 注册中心集群
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/21
 */
@Slf4j
public class Cluster {

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
            servers.add(server);
        });

        // 启动定时任务
        executorService.scheduleWithFixedDelay(() -> {
            // 定时探活所有注册中心节点
            updateServers();
            // 定时选主
            electLeader();
        }, 0, 5, TimeUnit.SECONDS);

    }

    private void updateServers() {
        // 请求每个实例的 /health 接口，更新实例的状态
        servers.forEach(server -> {
            try {
                Server instance = (Server) HttpInvoker.httpGet(server.getUrl() + "/health", Server.class);
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
    }

    public Server self() {
        return MYSELF;
    }

}
