# ZwRegistry 注册中心框架
## 打包部署
> jdk 17 maven 3.6+

```java
# 打包
mvn clean package
# 发布
mvn clean deploy -P release
```

## 实现功能
- [x] 服务者端
  - [x] 注册实例
  - [x] 取消注册实例
  - [x] 心跳保活
- [x] 消费者端
  - [x] 获取服务所有实例
  - [x] 订阅更新服务信息
- [x] 集成 RPC 框架 WmRPC
- [x] 服务探活
- [x] 注册中心集群
  - [x] 自动选主机制
  - [x] 同步 leader 信息
  - [x] 限制从节点只读，不可以在从节点修改数据

## 待完善
- [ ] 集成 WmRPC 框架，支持注册中心集群
- [ ] 从节点收到修改数据的请求后，自动请求主节点做数据修改操作
