# Guava学习
## 目录

[1.1 使用和避免Null(Optional)](#11-使用和避免nulloptional)

[1.2 Preconditions前置条件](#12-preconditions前置条件)

[2.1 不可变集合(add&remove直接抛出异常)](#21-不可变集合addremove直接抛出异常)

[2.2 Guava新集合涉略](#22-guava新集合涉略)

[2.3 集合工具类(java.util.Colletions扩展)](#23-集合工具类javautilcolletions扩展)
- [Java Collections常用](#java-collections常用)

- [Guava 集合工具类](#guava-集合工具类)

[2.4 Guava Cache](#24-guava-cache)

[2.5 字符串处理](#25-字符串处理)

---

## 1.1 使用和避免Null(Optional)
使用特殊值代替Null值让查找操作的语义更清晰；

如map.get(key)操作，如果key为Null或者key不在map集合内，则都会返回Null，这会造成混淆，语义不清；

jdk1.8新增的Optional借鉴Guava的Optional进行相应的Null值操作(方法返回值类型指定为Optional，则迫使调用者考虑*返回值引用缺失*的情形)。
## 1.2 Preconditions前置条件
用处：对方法的参数进行一些前置验证，避免if-else类型的验证代码

> 1. checkArgument: 根据第一个参数true或false验证参数
> 2. checkState: 检查对象状态
> 3. checkNotNull: 检查是否为空
> 4. checkElementIndex: 检查位置（顺序）是否有效(index <= size)
> 5. checkPositionIndex: 检查索引（下表）是否有效(index < size)

```Java
/**
 * 
 Guava PreCondition前置条件测试
 */
@Test
public void preConditionTest() {
    try {
        getPreConditionTest(null, 10);
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
    try {
        getPreConditionTest("Jasmiine", 1);
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
}

public static void getPreConditionTest(String name, Integer age) {
    Preconditions.checkNotNull(name, "姓名不能为空");
    Preconditions.checkArgument(age > 10, "%s > %s is error", age, 10);
}
```    
## 2.1 不可变集合(add&remove直接抛出异常)
注：集合元素为T实例时，集合还是可以发生变化的（浅层）


使用场景：初始化后不可变，只读的集合（防御性编程，多线程安全）

可变集合接口 | 不可变接口 
------------ | -------------
Collection | ImmutableCollection
List | ImmutableList
Set | ImmutableSet
Map | ImmutableMap
SortedSet | ImmutableSortedSet
SortedMap | ImmutableSortedMap
## 2.2 Guava新集合涉略
1. MultiSet: 可放重复元素，便于统计
2. MultiMap: 一个key对应多个value值，Spring中也有MultiValueMap(解决Map<K, List<V>>多值问题)
```Java
// MultiMap：map嵌套set，list
Map<String, List<String>> nestMap = new HashMap<>();
nestMap.put("J", Arrays.asList("J", "a", "s"));
nestMap.put("w", Arrays.asList("m", "i", "e"));
for (List<String> value : nestMap.values()) {
    System.out.println("value: " + value);
}

// Guava
Multimap<String, String> multimap = HashMultimap.create();
multimap.put("J", "J");
multimap.put("J", "a");
multimap.put("J", "s");
multimap.put("J", "m");
multimap.put("w", "l");
multimap.put("w", "a");
multimap.put("w", "d");
multimap.put("w", "e");
for (String w : multimap.get("w")) {
    System.out.println("w: " + w);
}

for (String l : multimap.get("l")) {
    System.out.println("l: " + l);
}

// Spring
MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
multiValueMap.add("J", "J");
multiValueMap.add("J", "a");
multiValueMap.add("J", "s");
multiValueMap.add("J", "m");
multiValueMap.add("w", "l");
multiValueMap.add("w", "a");
multiValueMap.add("w", "d");
multiValueMap.add("w", "e");
for (String w : multiValueMap.get("w")) {
    System.out.println("spring w: " + w);
}
// 内部无对应key，返回null
for (String l : multiValueMap.get("l")) {
    System.out.println("spring l: " + l);
}
```
3. Table: 可通过行，列确定value值(双层map)
```Java
Table<String, Integer, Double> table = HashBasedTable.create();
table.put("w", 1, 12.4);
table.put("J", 2, 12.3);
table.put("J", 1, 12.5);
System.out.println(table.row("J"));
```
4. ClassToInstanceMap: 它的键是类型，而值是符合键所指类型的对象
```Java
ClassToInstanceMap<Number> instanceMap = MutableClassToInstanceMap.create();
instanceMap.put(Integer.class, Integer.valueOf(10));
instanceMap.put(Float.class, 12.3f);

instanceMap.forEach((k, v) -> {
    if (k.equals(Float.class)) {
        System.out.println(v);
    }
});
```
5. RangeSet | RangeMap 一组不相连的、非空的区间
```Java
RangeSet<Integer> rangeSet = TreeRangeSet.create();
rangeSet.add(Range.closed(1, 10)); // {[1,10]}
rangeSet.add(Range.closedOpen(11, 15));//不相连区间:{[1,10], [11,15)}
rangeSet.add(Range.closedOpen(15, 20)); //相连区间; {[1,10], [11,20)}
rangeSet.add(Range.openClosed(0, 0)); //空区间; {[1,10], [11,20)}
rangeSet.remove(Range.open(5, 10)); //分割[1, 10]; {[1,5], [10,10], [11,20)}

System.out.println(rangeSet.toString());
// 补集
System.out.println(rangeSet.complement().toString());

Set<Range<Integer>> ranges = rangeSet.asRanges();
System.out.println("------------->");
for (Range<Integer> range : ranges) {
    System.out.println(range.toString());
    System.out.println(range.lowerEndpoint());
}
System.out.println("------------->");
```
## 2.3 集合工具类(java.util.Colletions扩展)

### Java Collections常用
1. 排序: sort, reverse, swap, rotate, shuffle......
    ```Java
    List<Integer> list = new ArrayList<>();
    list.add(8);
    list.add(2);
    list.add(-9);
    list.add(3);
    list.add(1);
    System.out.println(list);
    Collections.reverse(list);
    System.out.println(list);
    Collections.sort(list, (x, y) -> x.compareTo(y));
    System.out.println(list);
    Collections.shuffle(list);
    System.out.println(list);
    ```
2. 查找/替换；max, min, binarySearch......
    ```Java
    List<Integer> list = new ArrayList<>();
    list.add(8);
    list.add(2);
    list.add(-9);
    list.add(3);
    list.add(1);

    System.out.println(Collections.max(list));
    ```
3. 其他：disjoint(两集合没有相同元素返回true), addAll
4. 同步控制：synchronizedXXX方法（源码使用Synchronized代码块实现）
    ```Java
    Collections.synchronizedList(list);
    // 源码(Collections内部)
    ...
    public E get(int index) {
            synchronized (mutex) {return list.get(index);}
    }
    ...
    ```
### Guava 集合工具类
> Collections2, Lists, Sets, Maps, Queues, MultiSets, MultiMaps, Tables......
```Java
// 静态工厂方法(内部使用Collections.addAll)
List<Integer> integers = Lists.newArrayList(1, 2, 3, 4);
System.out.println(integers);
System.out.println(Lists.partition(integers, 2));
System.out.println(Iterables.getFirst(ImmutableList.of(1, 2, 4), 10));

// Iterables(Guava工具类更偏向于接受Iterable而非Collection)
Iterable<Integer> integerIterable = Iterables.concat(Ints.asList(1, 2, 3), Ints.asList(4, 5, 6));
System.out.println(integerIterable);
Integer first = Iterables.getFirst(integerIterable, -1);
System.out.println(first);
System.out.println(Iterables.limit(integerIterable, 2));
// Collection时实际执行size方法
System.out.println(Iterables.size(integers));
System.out.println(Iterables.size(integerIterable));
System.out.println(Iterables.partition(integerIterable, 2));
```
## 2.4 [Guava Cache](http://ifeve.com/google-guava-cachesexplained/)
本地缓存，获取缓存-如果没有-则计算[get-if-absent-compute]

![segment](https://github.com/Wliz/BooksNote/blob/master/%08images/segment.jpg)

适用条件：
> 1. 使用空间换取时间（速度）
> 2. 一些key可以被频繁查询
> 3. 存储部分数据（本地）

创建方式：CacheLoader(推荐使用)和Callable
```Java
// CacheLoader
 LoadingCache<String, WhiteList> whiteListLoadingCache =                CacheBuilder.newBuilder()
            .expireAfterAccess(CACHE_MINUTES, TimeUnit.MINUTES)
        .build(CacheLoader.from((key) -> whiteListMapper.selectByAppKey(key)));

// or
LoadingCache<String, WhiteList> whiteListLoadingCache =                 CacheBuilder.newBuilder()
            .expireAfterAccess(CACHE_MINUTES, TimeUnit.MINUTES)
            .build(new CacheLoader<String, WhiteList>() {
                @Override
                public WhiteList load(String key) throws Exception {
                    return null;
                }
            });

// Callable
Cache<String, WhiteList> whiteListLoadingCache = CacheBuilder.newBuilder()
    .expireAfterWrite(CACHE_MINUTES, TimeUnit.MINUTES)
    .build();    
try {
    whiteListLoadingCache.get(key, new Callable<WhiteList>() {
        @Override
        public WhiteList call() throws Exception {
            return whiteListMapper.selectByAppKey(key);
        }
    });
} catch (ExecutionException e) {
    e.printStackTrace();
}
```
```Java
LoadingCache<String, Integer> cache = CacheBuilder.newBuilder()
    // 缓存数量
    .maximumSize(10)
    // 缓存有效期5秒
    .expireAfterWrite(5, TimeUnit.SECONDS)
    .removalListener(notification -> System.out.println("key: " + notification.getKey() + " value: " + notification.getValue()))
    .build(CacheLoader.from(key -> -1));

// 只进行查询，未命中返回null
System.out.println("key1: " + cache.getIfPresent("key1"));
// 手动放入数据
cache.put("key1", 1);
System.out.println("key1: " + cache.getIfPresent("key1"));
System.out.println("size: " + cache.size());
// 手动注销缓存
cache.invalidate("key1");
System.out.println("key1: " + cache.getIfPresent("key1"));

try {
// 查询缓存，未命中，则加载
    System.out.println("key2: " + cache.get("key2"));
    System.out.println("size: " + cache.size());
    cache.put("key2", 5);
    System.out.println("key2: " + cache.get("key2"));

    // 连续插入10个数据
    for (int i = 3; i < 13; i++) {
        cache.put("key" + i, i);
    }
    System.out.println("size: " + cache.size());
    // 最近最少使用删除缓存
    System.out.println("key2: " + cache.get("key2"));
    // 等待5秒
    Thread.sleep(5000);
    // 超时失效后，size未更新
    System.out.println("size: " + cache.size());
    System.out.println("key3: " + cache.getIfPresent("key3"));
    // 操作的时候进行维护，而非单独启动线程维护，减少消耗
    System.out.println("size: " + cache.size());
    cache.put("key2", 22);
    System.out.println("size: " + cache.size());
    System.out.println("key2: " + cache.get("key2"));
    System.out.println("size: " + cache.size());

    Thread.sleep(5000);
    System.out.println("size: " + cache.size());
    System.out.println("key2: " + cache.getIfPresent("key2"));
    System.out.println("size: " + cache.size());
    // cache.refresh("key2");
} catch (Exception e) {
    e.printStackTrace();
}
```
缓存回收(是通过读写操作时顺便进行回收操作，而非单线程监测)：maxnumSize（容量回收），定时回收[expireAfterAccess读写, expireAfterWrite写], 还有引用回收

注：可以使用方法显示的插入数据put，删除数据invalidate，刷新数据refresh......

## 2.5 [字符串处理](http://ifeve.com/google-guava-strings/)
连接器（Joiner）,拆分器（Splitter），字符匹配器（CharMatcher），字符集（Charsets），大小写格式（CaseFormat）
```Java
// jdk字符串集合可以
System.out.println(Arrays.asList("1", "2").stream().collect(Collectors.joining(";")));
// Guava
System.out.println(Joiner.on(";").join(Arrays.asList(1, 2, 3, 4, 5)));
System.out.println(Joiner.on(";").join(Ints.asList(1, 2, 3, 4, 5)));
String str = ",a,,b,";
// Jdk 最后一个切分元素丢弃
System.out.println(Arrays.toString(str.split(",")));
System.out.println(Arrays.asList(str.split(",")));
// Guava保留
System.out.println(Splitter.on(",").omitEmptyStrings().split(str));

// CaseFromat
System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, "omitOmit"));
System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, "omitLmit"));
```
