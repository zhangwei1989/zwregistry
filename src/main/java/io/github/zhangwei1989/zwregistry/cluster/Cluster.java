package io.github.zhangwei1989.zwregistry.cluster;

import io.github.zhangwei1989.zwregistry.ZwRegistryConfigProperties;
import io.github.zhangwei1989.zwregistry.service.ZwRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

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

    private Server MYSELF;

    @Getter
    private List<Server> servers;

    public Cluster(ZwRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    public void init() {
        initServers();
        new ServerHealth(this).checkServerHealth();
    }

    private void initServers() {
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
    }

    public Server self() {
        MYSELF.setVersion(ZwRegistryService.VERSION.get());
        return MYSELF;
    }

    public Server leader() {
        return servers.stream().filter(Server::isLeader).findFirst().orElse(null);
    }

}
