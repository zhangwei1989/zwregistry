package io.github.zhangwei1989.zwregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * election
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/23
 */
@Slf4j
public class Election {

    public static void electLeader(List<Server> servers) {
        // 所有节点执行相同的选主逻辑，选出来的都是同一个主
        List<Server> masters = servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .toList();
        // 如果主节点大于 1 个，需要重新选主
        if (masters.size() > 1) {
            log.debug("more than one leader server, need to re-elect: {}", masters);
            doElect(servers);
        } else if (masters.size() == 0) { // 如果没有主节点，也需要选主
            log.debug("no leader server, need to elect: {}", masters);
            doElect(servers);
        } else { // 其余情况不需要选主
            log.debug("no  need for elect, current leader is : {}", masters);
        }
    }

    private static void doElect(List<Server> servers) {
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
            candidate.setLeader(true);
            log.debug("elected leader is {}", candidate);
        }
    }

}
