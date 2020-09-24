## Canal
阿里开源基于mysql的binlog增量订阅和消费组件，通过订阅数据库的binlog日志，进行一些数据消费；

canal基于mysql的主从同步原理，冒充slave从库，监听主库的binlog日志，然后进行解析处理（如对ES进行增量更新）；

官方提供了一个canal adapter组件，将canal server获取的数据转换成redis，rocketmq,es等常见的数据源；