package io.github.zhangwei1989.zwregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * elect for leader
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/22
 */
@Slf4j
public class Election {

    public void electLeader(List<Server> servers) {
        List<Server> masters = servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .collect(Collectors.toList());
        if (masters.isEmpty()) {
            log.debug(" ======> no master, need to elect : {}", servers);
            elect(servers);
        } else if (masters.size() > 1) {
            log.debug(" ======> more than one master, need to re-elect : {}", servers);
            elect(servers);
        } else {
            log.debug(" ======> no need election for leader : {}", masters.get(0));
        }
    }

    private void elect(List<Server> servers) {
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

}
