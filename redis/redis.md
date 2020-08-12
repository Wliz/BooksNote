# Redis
## Redis简介

Redis是遵守BSD协议，高性能的key-value数据库

- Redis支持数据持久化，内存数据保存磁盘，可以重启再次加载
- Redis支持主从数据备份
- Redis支持多种数据类型如list，set，zset，hash, String.
- Redis读速度达到11w/s，写8.1w/s
- Redis所有操作都是原子性，支持多个操作合并后原子性执行
- Redis支持订阅发布（publish/subscribe），key过期等

## Redis数据类型

string(字符串)，hash（哈希），list（列表），set(集合)，zset（sorted set有序集合）

### string（字符串）

string是最基本类型，一个key对应一个value，一个键最大存储512MB；

对应处理命令：
- set key value [expiration EX seconds | PX milliseconds] [NX | XX]
- get key(获取key值信息)
- del key（删除key）
- getrange key start end(获取字符串子串)
- getset key value(设置新值并返回旧值)
- getbit key offset(返回键处存储的字符串值中偏移处的位值)
- mget key1[key2 ...](mul获取所有键的值)
- setbit key offset value(设置偏移位置的值)
- setex key seconds value(使用键和到期时间设置值)
- setnx key value(当键不存在时设置值)
- strlen key(元素长度)
- setrange key offset value(从offset处覆盖)
- mset key value[key value ...](设置多key 多value)
- msetnx key value [key value](当key不存在时设置value)
- psetex key milliseconds value(设置键的值和到期时间)
- incr key(键的整数值+1,值为整数值才可以)
- decr key(整数值-1)
- incrby key increment(指定增加值)
- incrbyfloat key increment(浮点数增加)
- decrby key decrement（整数减少指定）
- append key value
```
✗ redis-cli
127.0.0.1:6379> set name 'runoob'
OK
127.0.0.1:6379> get name
"runoob"
```

### hash(哈希)

hash是一个键值对的集合，是一个string类型的字段和值的映射表，特别适合用于存储对象；
每个hash可以存储2^32-1键值对（40亿+）;

对应处理命令：
- hmset key field value [field value ...]
- hgetall key
- hkeys key 获取field值
- hvals key 获取value值
- hdel key field [field ...]
- hexists key field 字段是否存在
- hincrby key field increment(整数增加)

```
127.0.0.1:6379> hmset user:1 username runoob password runoob points 200
OK
127.0.0.1:6379> hgetall user:1
1) "username"
2) "runoob"
3) "password"
4) "runoob"
5) "points"
6) "200"
```

### list(列表)

list是简单的字符串列表，按照插入顺序排序，也可以从头部和尾部插入；

列表可以存储2^(32-1)个元素（40亿+）；

列表为链表结构，首尾操作性能高，随机读写性能较差；

对应命令：
- lpush key value [value ...] 左进右出（多值）
- brpoplpush source target timeout 从list中最后一个元素弹出并放入到另外一个list集合中，否则等待超时
- lpushx key value (x代表key存在时)
- rpush key value [value ...] 右进左出（多值）
- rpushx key value 
- lpop key 左出
- rpop key 右出
- lrange key start stop 获取
- llen key 长度
- lindex key index 访问指定位置元素
- lset key index value 设置指定位置值
- linsert key before|after pivot value插入
- lrem key count value 删除元素
- ltrim key start stop 定长列表
```
127.0.0.1:6379> lpush oob redis mongodb
(integer) 2
127.0.0.1:6379> lrange oob 0 10
1) "mongodb"
2) "redis"
```

### set(集合)

string类型的无序集合，通过哈希表实现，添加，删除，查找复杂度都是O(1);

set集合无重复，内部元素具有唯一性，多次插入后续插入被忽略；

对应命令：
- sadd key member [member ...]
- smembers key
- scard key 获取长度
- srandmember key 获取随机其中一个值
- srem key member [member 。。。] 删除元素
- sismember key member 判断元素是否存在

### zset(sorted set有序集合)

zset和set一样string类型元素集合，内部元素不允许重复；

zset每个元素关联一个double类型分数score，通过该值对集合元素进行排序；元素不可重复，分数score可以重复；

对应命令：
- zadd key [NX | XX] [CH] [INCR] score member [score member ...]
- zrangebyscore key min max [withscores] [limit offset count] (-inf，+inf代表负无限，正无限)
- zrange key start stop withscores
- zrevrange key start stop [WITHSCORES]
- zcard key 获取zset集合长度
- zrem key member [member ...] 删除元素
- zincrby key increment member 增加值
- zscore key member 获取score排序
- zrank key member 正向排序
- zrevrank key member 获取指定元素的反向排名
- zremrangebyrank key start stop 按照score排名删除范围元素
- zremrangebyscore key min max

## redis命令

redis命令用于在redis服务器上执行相关操作;

对应命令：
- redis-cli -h host -p port -a password(链接远程服务器)
- del key(删除指定key若存在)
- dump key(返回存储在指定键的值的序列化版本)
- exists key(检查对应key是否存在)
- expire key seconds（设定过期时间对key）
- expireat key timestamp（设定key在时间戳过期，Unix时间戳格式秒）
- pexpire key milliseconds (设定过期时间毫秒)
- pexpireat key milliseconds-timestamp(unix时间戳设置过期时间毫秒)
- keys pattern（查找与指定模式匹配的所有键）
- move key db(转移到另外数据库)
- persist key（删除键的过期时间，永生）
- pttl key（获取过期剩余时间）
- randomkey(返回一个随机键)
- rename key newkey(重命名)
- renamenx key newkey(新key不存在，重命名，否则失败)
- type key(返回键的值类型)

## 发布/订阅

redis的发布订阅（pub/sub）是一种消息通信模式，发布者发布pub消息，订阅者sub接受消息；两者之间传递消息的链路称为信道，redis中客服端可以订阅任意数量的信道；

TODO:
- 发布订阅是否只是针对一个订阅者(非)

发布订阅针对通道channel来说，可以存在多个发布者发布信息到单个channel中，并且消费者可以订阅多个不同通道处理；

命令：
- publish channel message(发布)
- subscribe channel [channel ...](订阅)

## 事务

redis事务允许在单个步骤执行一组命令，但仍然是顺序执行，在此期间无法向其他客户端发送消息；

由multi命令启动，exec命令执行；