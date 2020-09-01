# CAP理论

1. 一致性（Consistency）

一致性指 all nodes see the same data at the same time,更新操作操作成功并返回客户端后，所有节点在同一时间的数据完全一致；

数据一致性解决方案：
- 两阶段提交（2PC two Phase commit）
    
    两阶段提交协议是一种分布式算法，用于协调参与分布式原子事务的所有进程，以保证他们均完成提交或中止（回滚）事务；

    2PC是一个阻塞协议，服务在投票后需要等待协调器的决定，此时服务会阻塞并锁定资源。

    - 投票阶段:协调器向所有服务发起投票请求，服务回答yes or no，如果有服务回答no或者拒绝，超时等，协调器在下一阶段发送中止消息
    - 决定阶段：所有服务回复yes，协调器发送commit消息，接着服务服务告知事务完成或失败；如果任何服务提交失败，协调器启动额外步骤以中止该事务。
- Tcc(try -confirm/cancel)

    补偿型事务模式，支持两阶段的商业模型：

    - 尝试阶段，将服务置于待处理状态
    - 确认阶段：将服务设置为确认状态

2. 可用性（Availability）

可用性指 Reads and writes always succeed, 读写服务一直可用;

3. 分区容错性（Partition tolerance）

the system continues to operate despite arbitrary message loss or failure of part of the system, 分布式系统在遇到某节点或网络分区故障时，仍然能够对外提供满足一致性和可用性的服务；

CAP理论三者只能同时满足这三项中的两项，具体哪种要根据场景来确定，比如钱财的，C（一致性）要保证。

# BASE理论

BASE理论是对CAP的延伸， 核心思想是即使无法做到强一致性（Strong Consistency），但应用可以采用适当的方式大道最终一致性（Eventual Consistency）.

1. 基本可用（Basically Available）

分布式系统在出现故障时，允许损失部分可用性，保证核心可用。

2. 软状态（Soft State）

允许系统存在中间状态，而该中间状态不影响系统整体可用性。

3. 最终一致性（Eventual Consistency）

数据副本经过一定时间之后，最终能够达到一致的状态