## 获取问题SQL

不同数据库有不同的获取方法：
 - mysql
    - 慢查询日志（使用最多）
    - 测试工具loadrunner
    - Percona公司的ptquery等工具
## SQL优化

优化分为两步，包括分析和修改；分析就是对相应的问题sql语句执行执行计划explain sql；

explain执行结果字段解析：

| 字段 | 解释 |
| - |-|
| id | 每个被独立执行的操作标识，标识对象被操作顺序；序号越大，越先被执行，如果相同，从上到下|
| select_type | 查询中select语句类型 |
| table | 被操作的对象名称，通常是表名 |
| partitions | 匹配的分区信息，非分区表信息为null |
| type | 连接操作类型 | 
| possible_keys | 可能用到的索引 | 
| key | 优化器实际用到的索引【最需要关注列】，效果从最好到最差：const -> eq_ref -> ref -> range -> index -> all(全表)|
| key_len | 被优化器选定的索引长度，字节 | 
| ref | 本行被操作对象的参照对象，若无则为null；表查找时所用到的列和常量，有const和列名 |
| rows | 查询执行所扫描的元组个数（此值为估计值） |
| filtered | 条件表上数据被过滤的元组数据的个数百分比 |
| extra | 执行计划的补充信息，当此列出行using filesort或者Using temporary时，很可能需要优化 | 

select_type:

- SIMPLE:简单select
- PRIMARY：最外层select
- UNION：union中的第二个或者后面的select
- DEPENDENT UNION：union中第二个或者后面的select，取决于外面查询
- UNION RESULT：union结果
- SUBQUERY：子查询第一个select
- DEPENDENT SUBQUERY：子查询第一个select
- DERIVED：导出表的select（from子句的子查询）

type（链接类型）：
最好到最差情况
- system(const连接类型特例) 表的一行；
- const：表最多有一个匹配行，将在查询开始时读取；只读取一次；
- eq_ref：对每个来自前面表的行组合，从该表中读取一行【用在索引为UNIQUE或PRIMARY KEY】
- ref：对每个来自前面表的行组合，所有匹配索引值的行从该表读取【非UNIQUE， PRIMARY KEY】
- ref_or_null -> index_merge -> unique_subquery -> index_subquery -> range -> index -> ALL）

key（优化器实际使用的索引）：可以强制mysql使用索引或者忽略索引【FORCE INDEX，USE INDEX，IGNORE INDEX】

一般优化策略：索引优化，sql改写，参数优化，优化器；

注：mysql使用limit offset,n，并不是跳过offset行，而是取offset+n行，放弃前offset行，返回n行，所以当offset特别大时，效率比较低；



## 索引hash，b+ tree?
https://www.cnblogs.com/williamjie/p/11187470.html


### 索引是什么？
索引是一种数据结构，能够帮助我们快速的检索数据库中的数据。比如Hash索引和B+ Tree索引；

### 索引具体采用哪种数据结构呢？
常见的MySQL索引主要有两种数据结构：Hash索引和B+ Tree索引；mysql innodb引擎默认B+ Tree索引；

### 那你讲一下为什么使用B+索引，和Hash索引比较起来有什么优缺点？

Hash索引的底层结果是Hash表，哈希表是一种key-value的存储结果，多个数据在存储关系上没有任何顺序关系，所以适合等值查询，范围区间查询无法通过索引查询，需要全表扫描；

而B+ Tree树是一种多路平衡查询树，天然有序，左节点小于父节点，右节点大于父节点，范围区间查询可以通过索引，无须全表扫描；

另外总结一下：

1. Hash索引适用于等值查询，不适合范围取件查询；
2. Hash索引无法通过索引排序；
3. Hash索引不支持多列组合索引的最左匹配原则；
4. Hash索引在大量key值重复的时候，会出现哈希碰撞，查询效率变低；

### B+Tree的叶子节点可以存储那些数据？

叶子节点可以存储整行数据，也可以是主键的值；

叶子节点存储整行数据的是主键索引，又称聚簇索引；存储主键的值是非主键索引，是普通索引；

聚簇索引因为叶子节点存储了整行数据，所以查询一次就可以得到需要的数据，无须回表查询；而普通索引需要两次查询，第一次查询得到主键的值，第二次通过主键的值查询具体数据（又称回表查询）；

普通索引中组合索引如果查询索引字段，同样不需要二次回表定位查询，所以并不是所有的非主键索引都要查询两次（覆盖索引）；

### mysql索引结构

https://www.cnblogs.com/wangflower/p/12237762.html

