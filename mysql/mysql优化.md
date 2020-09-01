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
