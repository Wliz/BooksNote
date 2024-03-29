# db主从搭建
为了保证存储层面db的高可用,需要对db进行异地多活,主从部署等;

异地多活是将db服务部署在不同地域,避免因为机房单点故障或者自然原因导致的db异常,无法提供服务;

# 一主多从搭建
利用MySQL的binlog日志,可以为主节点部署一个或多个从节点,达到数据读写分离,降低服务器压力;

原理:
1. master节点在数据发生变化,将根据mysql的数据复制方式(按照语句复制,按照行复制,混合复制)将变动写入binlog日志中;
2. master在接收到从节点同步请求后,将变动日志binlog发送给从节点,从节点将接受的binlog日志写入中继日志,并另起线程进行重放即可完成数据同步;

优点:
1. 读写分离: 降低主节点压力,核心业务使用主库,非核心读业务从库,提高服务性能;
缺点:
1. 主从同步存在时间差,对核心业务不友好;
2. 从节点过多会导致主库性能差(主库需要启动线程完成所有从节点日志同步);
3. 只有一个主库节点,发生故障后无法切换,可用性并不高;

# 多主多从

相比一主多从的部署方式,多主多从主要解决单主节点可靠性不高的缺点,当主节点发生故障后,可以进行切换到其他主节点,提供极高的可靠性;

但主从同步时间差和从节点过多影响性能的缺点无法解决;相对于单主来说,多主利大于弊;

多个主节点之间互为主从,保证一个主节点数据更新后,其他主节点也能得到更新;

总结:
1. 服务通过提供主从部署提供存储服务的稳定性;
2. 读写分离一般分为代码层(侵入业务,识别sql语句的select走从库)与中间代理层(统一处理,对外暴露单个ip,使用方无感知,需团队维护);

资料:
1. 一主多从: https://www.cnblogs.com/qdhxhz/p/14044692.html
2. 多主多从: https://blog.csdn.net/code727/article/details/84745719?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-1.no_search_link&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-1.no_search_link
3. 主从同步链接ing解决方式:
    - 检查ip,端口,密码,账号是否正确,能否登录;
    - 判断pod和log_file是否正确,不正确进行修改从库;
    - 如果仍无法解决,尝试跳过已有的同步计数:-- 解决无法同步,跳过slave统计信息SET GLOBAL SQL_SLAVE_SKIP_COUNTER=1;
