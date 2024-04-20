package io.github.zhangwei1989.zwregistry.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 注册中心实例
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "url")
public class Server {

    private String url;

    private boolean status;

    private boolean leader;

    private long version;

}
