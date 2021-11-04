# JDK命令行工具
- jps: 类似linux的ps指令,用于查看所有java进程的启动类,启动参数以及java虚拟机参数;
- jstat: 用于虚拟机HotSpot运行时数据;
- jinfo: 显示虚拟机配置信息;
- jmap: 生成堆转储快照,用于分析堆中数据;
- jhat: 分析堆转储快照,会建立http服务,通过浏览器查看分析结果;
- jstack: 生成虚拟机当前线程栈快照(虚拟机中运行的线程的堆栈快照信息);

## jstack
jstack + thread_id可查找死锁,可以用jvisualvm自动检测死锁;

jstack照出占用CPU最高的线程堆栈信息: 使用top -p <pid>显示java进程内存情况,再按H,获取线程的内存情况;

如jstack 19663 | grep -A 10 4cd0,得到线程堆栈信息中4cd0线程所在行的后面10行,从堆栈发现CPU飙高的调用方法;


# JDK可视化分析工具

- JConsole: 可远程连接正在运行的虚拟机,进行监视管理;(需要远程服务器开启端口等)[对线上服务影响大];
- Visual VM:多合一故障处理工具(对线上服务影响小);
- Arthas(阿里提供)分析堆转储快照;

资料:
- https://github.com/Snailclimb/JavaGuide/blob/master/docs/java/jvm/JDK%E7%9B%91%E6%8E%A7%E5%92%8C%E6%95%85%E9%9A%9C%E5%A4%84%E7%90%86%E5%B7%A5%E5%85%B7%E6%80%BB%E7%BB%93.md