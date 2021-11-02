## redis和memcach有什么区别？高并发下有时单线程的redis比多线程的memcached效率高？
- memcach可以缓存key-value，redis支出除k/v更多的数据结构
- memcach使用的都是物理内存，而redis由于其内部机制，支持虚拟内存；另外redis还支持可持久化，aof备份文件恢复以及主从数据备份等。
- redis还支持消息队列（如主题订阅，一个生产者，多个消费者，就是消费者挂掉会出现数据丢失）

memcached使用多线程，子线程执行任务，主线程监听，在多线程之间引入锁，加锁带来部分性能损耗；

## redis如何实现主从复制

主节点将内存中数据做一份快照，并将数据快照发送给从节点（数据同步有网状和线性同步，建议线性同步【可以不说类型】），然后从节点将数据加载到内存中恢复；再之后主节点增加数据，主节点采用类似mysql二进制日志方式将更新语句发送到从节点，然后从节点进行更新；

## redis的key如何寻址

使用路由查询分片：（具体我不理解）

## redis如何实现分布式锁，实现思路？zk如何实现以及实现方式区别？

redis实现思路（如redisson）：【需要去网络查询文档处理，好像又好几种方式】
todo

注：
集群下使用Redisson分布式锁： https://blog.csdn.net/weixin_44565095/article/details/100598965?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.channel_param&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.channel_param

https://mp.weixin.qq.com/s?__biz=MzU5ODUwNzY1Nw==&mid=2247484155&idx=1&sn=0c73f45f2f641ba0bf4399f57170ac9b&scene=21#wechat_redirect

## redis持久化，底层实现，优缺点

RDB（Redis DataBase）：在不同的时间点将redis的数据生成快照并同步到磁盘上，并定期更新磁盘快照；缺点（消耗时间，性能（fork+io），易丢失数据）
[todo 有时间了解redisRDB实现如fork子线程和io，如何说易丢失数据]

AOF(Append Only File): 将redis执行过的指令记录，并在redis重启时，执行记录的命令就可以，写日志；缺点：体积大，恢复速度慢；

bgsave做镜像全量持久化，AOF做增量持久化；bgsave会消耗很长时间，不够实时，停机时会导致大量数据丢失，需要AOF配合；在redis重启时，优先使用aof文件恢复内存状态，如果没有aof，则使用rdb文件恢复；

另外redis会做定期aof重写，并压缩aof文件日志大小；在redis 4.0之后官方增加了混合持久化功能，将bgsave和aof增量做了融合，既保证恢复效率有兼顾数据安全；

bgsave原理：fork和cow，fork是redis通过创建子进程进行bgsave操作，cow指copy on write，子进程创建后，父子进程共享数据段，父进程继续提供读写服务，写的脏的页面数据会组件和子进程分离开来；

## redis过期策略那些，LRU算法了解吗，说说Java代码实现

过期策略有三种：
- 定时过期（一key，一定时器）
- 惰性过期：使用key时才判断key是否过期，过期则清理
- 定期过期：两者折中（todo，需要去查询具体细节）

淘汰策略：https://www.jianshu.com/p/8aa619933ebb

淘汰策略共有6种，分为两类对应全部key和设置了过期时间的key
- 内存满时，写入操作报错；
- 内存满时，写入操作时删除最近最少使用key；
- 内存满时，写入操作会随机删除一个key；
- 内存满时，从设置过期时间的键中移除最近最少使用的key；
- 内存满时，从设置过期时间的键中随机移除一个；
- 内存满时，从设置过期时间的键中找到快要过期的key删除；

https://thinkwon.blog.csdn.net/article/details/103522351

LRU（Least recently used最近最少未使用）：如Java的LinkedHashMap(capacity, DEFAULT_LOAD_FACTORY, true);

// 为true时代表按照访问顺序排序，作为LRU缓存；为false时，按照插入顺序，为FIFO

LRU算法实现：
- 通过双向链表实现，新数据插入链表头部
- 缓存命中将该数据移动到链表头部
- 当链表满时，将链表尾部数据删除即可（最近最少未使用原则）

LinkedHashMap: HashMap和双向链表合二为一就是该类，HashMap是无序，LinkedHashMap通过维护一个额外的双向链表保证迭代顺序，可以是访问顺序，也可以是插入顺序；

## 什么是缓存缓存穿透，缓存击穿，缓存雪崩以及对应的解决方案？

- 缓存穿透：指查询一个不存在的数据，缓存未命中，去db拉取并更新缓存；如果有大量不存在数据查询，或拖垮db；
    - 解决方案：
        - 第一种将查询的不存在数据缓存起来，value为null，过期时间设置很短，避免给db带来压力（同样的key）
        - 第二种采用一个很大的记录存在数据的布隆过滤器bitmap，进行布隆过滤（不同的key场景）；
- 缓存击穿：设置过期时间的key数据在过期时间点过期，而在过期时间点大量查询该key的请求过来，会去db查询，导致拖垮db，造成数据库宕机；
    - 解决方案：
        - 第一种使用互斥锁，缓存失效时，非立即load db，使用setnx设置锁，操作成功获取锁load db并更新缓存；否则重试get缓存
        - 缓存永不过期：物理缓存不过期，逻辑过期（后台异步线程刷新缓存）
- 缓存雪崩：设置缓存时采用了相同的过期时间，导致缓存中大量缓存数据同时过期，请求全部转发到db，db压力过重雪崩；与击穿相比是击穿是单个key，雪崩为多key同时失效
    - 解决方案：
        - 将不同key的过期时间分散开，避免同时失效，可以在统一时间基础上增加随机时长，降低引发大量key同时失效


 布隆过滤器（Bloom Filter）：一个长度为m比特位的位数组（bit array）与k个hash函数（hash function）组成的数据结构，位数组初始化均为0，所有hash函数都可以分别把输入数据尽量均匀散列；

 当插入一个元素时，将数据分别输入k个hash函数，产生k个hash值。以hash值作为位数组中的下标，将所有k个对应的比特置为1；

 当查询（判断一个数据是否存在）一个元素时，同样将数据输入k个hash函数，检查对应的k个比特位。如果出现任意一位为0，表明元素一定不在集合中；如果全为1，表明有很大可能在集合中，为什么说是不一定在呢，因为一个bit位置为1可能会受到其他元素的而影响，即假阳性；假阴性在bf中不会出现；

 优点：
 - 不存储数据本身，只用bit表示，占用空间小，保密性好；
 - 时间效率高，插入和查询时间复杂度o(k);
 - hash函数之间相互堵路，可以在硬件指令层面并行计算；

 缺点：
 - 存在假阳性概率，不适用用要求100#准确率的情景；
 - 只能插入和查询元素，不能删除元素；

 guava中布隆过滤器BloomFilter，其中默认的容错率为0.03左右

 https://blog.csdn.net/nrsc272420199/article/details/106366583


## 选择缓存时，选择redis，还是memcached？

选择redis场景：

- 复杂的数据结构：value数据为hash，列表，集合，有序集合时，选择redis，因为memcached无法满足该数据结构
- 需要数据持久化功能【有点懵逼】
- 高可用时，redis支持集群部署，实现主从复制，读写分离，对于memcached实现高可用，需要二次开发
- 存储内容大时，memcached单个value存储1m；redis中list单个元素512m，set单个元素512m等；

选择memcached场景：

- 存粹K/v结构，数据量大的业务；（原因如下）
    - memcached采用预分配内存池管理方式，能够省去内存分配的时间；redis是临时申请，可能导致碎片化
    - 虚拟内存使用：memcached将所有数据存储到物理内存，而redis采用vm机制，理论上可以存储比物理内存更多的数据，当数据超量时，引发swap，redis将冷数据刷新到磁盘；
    - 网络模型：memcached和redis均采用非阻塞IO复用模型，但redis提供非k-v存储之外的排序，聚合以及复杂的cpu计算等，会阻塞io，所以memcached更快；
    - 线程模型：memcached使用多线程，主线程监听，work子线程接搜请求，执行读写，该过程可能存在锁冲突，redis使用单线程，难易利用多核cpu优势；

## 缓存和数据库不一致怎么办

假设使用主从复制，读写分离的数据库，那么线程A先删除缓存，并将数据更新到主库中，而主库更新数据还未更新到从库时，线程B加载数据失败，load从库，并回设缓存，导致缓存中数据为旧数据；

主要原因就是主从数据同步中间有时间差，加入缓存后，导致不一致时间变长了（缓存回设）；

解决方案：
在从库更新数据后，将缓存数据同时更新，即从库数据更新后，通知缓存删除该数据即可；

## 主从数据库不一致时如何解决？

主从数据库数据不一致这个问题主要是主从数据库有同步时差导致数据不一致；

解决方案：
- 如果不是对数据的强一致性要求很高，只需要关注数据最终一致性即可；
- 强制读主库，从库只做数据备份，贾黄村提升读取性能；
- 选择性读主库，添加缓存，记录必须读主库数据，缓存设置过期时间为同步时间差，将读的那个库作为key，读数据时如果缓存有效则从主库读，无效从从库读；（不是太理解）

## redis中那些数据结构

字符串String， 字典表Hash， 列表List， 集合Set，有序集合SortedSet.

中高级用户要了解HyperLogLog，Geo，Pub/Sub主题订阅；

## Reis中有1亿key，其中10w key是某个固定前缀开头，如果将他们找出来？

可以使用keys指令查找出执行模式的key列表；

后续：如果redis服务正在给线上提供服务，使用keys会出问题？出问题是什么问题？

redis是单线程服务（新版本支持多线程，可能需要了解），keys会导致线程阻塞一段时间，服务停滞，直到keys执行结束；这个时候可以使用scan指令，可以无阻塞的提取出对应key列表，但会有重复概率，所有需要在客户端去重，整个时间比keys要长；
https://www.runoob.com/redis/keys-scan.html

## redis如何做延时队列

使用sortedSet，使用时间戳做score，消息内容作为key，zadd指令生产消息，消费者使用zrangebyscore获取n秒之前的数据做轮询消费；

String：如果存储数字的话，是用int类型的编码;如果存储非数字，小于等于39字节的字符串，是embstr；大于39个字节，则是raw编码。
List：如果列表的元素个数小于512个，列表每个元素的值都小于64字节（默认），使用ziplist编码，否则使用linkedlist编码
Hash：哈希类型元素个数小于512个，所有值小于64字节的话，使用ziplist编码,否则使用hashtable编码。
Set：如果集合中的元素都是整数且元素个数小于512个，使用intset编码，否则使用hashtable编码。
Zset：当有序集合的元素个数小于128个，每个元素的值小于64字节时，使用ziplist编码，否则使用skiplist（跳跃表）编码
