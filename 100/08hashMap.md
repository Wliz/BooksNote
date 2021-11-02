# HashMap 源码分析

HashMap实现Map接口，允许null键和值；非同步，不保证有序，也不保证顺序不随时间变化；

HashMap中以Node数组实现的哈希桶数组，用key的哈希值与数组下标最大值(length - 1)得到元素对应数组下标，等价于取余；

注：减一是为了防止数组下标越界，数组长度为2的幂，所以二进制为1000000……，先减一得到01111111111……，得到的结果一定不会比数组长度大，不会越界；减一导致与的对象为奇数，保证元素分布更加均匀；

取模运算和与运算得到相同结果：
(n - 1) & hash  =  n % hash

https://zhuanlan.zhihu.com/p/103282858

HashMap中有两个重要参数，capacity(容量)和loadFator（负载因子）；

```

The capacity is the number of buckets in the hash table, The initial capacity is simply the capacity at the time the hash table is created.

capacity 是哈希表中bucket的数量，初始容量是创建哈希表的容量；

The load factor is a measure of how full the hash table is allowed to get before its capacity is automatically increased.

负载因子是哈希表自动增加容量达到满度的一种量度；

重要参数：

- DEFAULT_INITIAL_CAPACITY = 1 << 4 : 位运算默认初始值大小16(位运算32位,高低各16位,进行hash,所以初始化大小为16)
- MAXIMUM_CAPACITY = 1 << 30 : 最大容量
- DEFAULT_LOAD_FACTOR = 0.75f : 默认负载因子
- TREEIFY_THRESHOLD = 8 : 超过该值时bucket转红黑树【同时要求数组长度也要大于等于64】
- UNTREEIFY_THRESHOLD = 6 : bucket取消树化的阈值
- MIN_TREEIFY_THRESHOLD = 64 : 当数组长度大于64时才考虑将链表转换为红黑树

源码注释中强调如果对迭代性能要求高，则不要将capacity设置过大，load factor设置过小；当bucket中entries数目大于capacity * load factor时需要调整bucket大小为当前的两倍。

设定初始值时容量为最接近的2的次方:
```Java
    // 位运算,效率高(在时间轮中大小设置时使用循环,不如这个效率高)
    /**
     * Returns a power of two size for the given target capacity.
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
```

注: 

1. HashMap为了降低元素之间的冲突,使用的hashcode使用了key的hash与hash的高16位做异或运算,使之分散均匀;
2. 内部类每个元素的hash使用的是key的hash与value的hash异或结果作为Node的hashcode使用;
3. 若初始化hashMap时指定大小capacity,则会按照接近2的次幂作为初始容量如初始值为3,实际大小为4;
4. HashMap初始化时若不指定大小,会进行懒加载创建,而非立即创建;
5. java8之前hash冲突加入链表使用头插法,时间局限性(认为后插入数据会先被访问,遍历访问会更快);后续使用尾插法,避免扩容情况下链表成环问题[多线程情况下];


## put分析

处理过程：

1. 对key做hash，然后计算对应数组下标index元素
2. 如果对应index没有hash冲突，则直接放入对应的bucket中（第一个元素）
3. 如果hash冲突（碰撞），以链表形式存储bucket中
4. 如果链表过长（binCount >= TREEIFY_THRESHOLD - 1），同时还要判断集合长度是否小于64，若小于则扩容，否则转换链表为红黑树
5. 最后一系列处理后，判断节点是否存在（不管是否是新节点还是旧节点），用value替换并返回
6. ++size > threshold 超了，则重置哈希表

```Java
 final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        // node节点集合为空或者长度为0代表没有元素，则新建node并设置默认大小
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        // 计算index下标元素是否为空，为空则新建相应node并赋值到index下
        // 为p赋值为对应下标元素
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            // 对应元素不为空，表示该下标有元素，就需要将新put的元素放入bucket桶内部（链表）
            Node<K,V> e; K k;   
            // 判断插入值和已有值hash，key是否相同
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
            // 完全相同直接替换
                e = p;
            else if (p instanceof TreeNode)
            // 判断是否是树，是树就放入桶内
            // 节点hash相同，但key不同
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                // hash不同
                for (int binCount = 0; ; ++binCount) {
                    // 下一个节点为空，代表是末尾节点，就继续添加
                    // p已经赋值为该hash对应的下标元素
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    // 表示p.next还有元素，不在桶的末尾，且正好该节点的元素和要put的元素一直，则直接退出，不设置，否则向后遍历p = e
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            // 此时e的value代表新的值或者老的值，用新值给替换掉，是为了替换掉循环里如果桶内key和hash对应上的数据
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                // onlyIfAbsent 此时false，需要替换
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                    // Callbacks to allow LinkedHashMap post-actions
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        // size代表哈希表内包含的key-value映射数量
        // threshold注释：The next size value at which to resize (capacity * load factor).
        // threshold代表需要重置的大小
        if (++size > threshold)
        // 若超过阈值，则进行容量调整（1.8调整为2倍）
            resize();
            // Callbacks to allow LinkedHashMap post-actions
        afterNodeInsertion(evict);
        return null;
    }
```

## CurrentHashMap

CurrentHashMap在1.7的时候采用分段锁segment实现并发；而1.8使用cas+synchronized保证并发，内部仍然存在Segment，为了保证序列化的兼容性；

### JDK7
https://juejin.cn/post/6844904136937308168
Segment数组和HashEntry数组组成;Segment为可重入锁,是一种数组和链表结构,来控制ConcurrentHashMap的并发程度,并发度初始化后无法再次扩容更改;
Segment类为ReentrantLock子类,内部含有一个HashEntry数组(可以理解为含有一个HashMap),每一个segment都是实现了Lock功能HashMap;ConcurrentHashMap中有一个segmen数组,进而实现控制并发;
```Java
static final class Segment<K,V> extends ReentrantLock implements Serializable {     transient volatile HashEntry<K,V>[] table; //包含一个HashMap 可以理解为}
```

- segment数组大小为2的次幂;
- segment中的table大小为2的次幂;
- concurrencyLevel: 并发度,默认16,分段锁个数,不产生锁竞争的最大线程数;初始化后不可调整;并发度设置过大,使原本位于同一个段内的数据分散到多个段内,CPU命中率会下降,引起程序性能下降;
- size方法先无锁统计所有数据量前后两次数据是否一致,一致则返回,否则对全部segment加锁统计,解锁;
- rehash时segment数组不可变化,每个segment内部的table数组可以扩容为2倍;

注: 
- ConcurrentHashMap允许多个修改操作并发进行,使用锁分离(分段锁),只要修改操作发生在不同段上,就可以并发进行;
- 分段Segment中的HashEntry数组元素的next均是volatile修饰,保证元素变量可见性;
- ConcurrentHashMap的弱一致性体现在迭代遍历是先segment后内部元素,会存在其他线程修改segment内部元素, clear方法也存在该问题;get方法与containsKey均是不加锁的遍历,对新添加的元素无法保证一致性;

### JDK8

Node<k,v>[] table : table默认大小为16，存储node节点，扩容时扩大两倍； Node中key和hash不可变，value和next是volatile，保证线程可见性；

Node<k, v>[] nextTable : nextTable默认null， 扩容时新生成的数组，大小为原来的2倍；

int sizeCtl: sizeCtl默认0，用来控制table的初始化和扩容操作 ，-1表示初始化或resized，-（1+正在重置线程数）正在被多个线程扩容；

volatile long baseCount : 真个hash表存储的节点总和;

内部构成:
- Node类:元素基本类;
- TreeNode: 红黑树节点;
- ForwardingNode: hash为-1时,说明该节点不需要resize;
- TreeBin: 存储树型结构的容器,封装TreeNode的容器,提供转换红黑树的条件和锁的控制.



### CurrentHashMap如果实现多线程的支持？

在initTable时通过变量sizeCtl实现只有一个线程在初始化，实现方式：
- 定义为volatile，线程间可见
- compareAndSwapInt，cas操作设置sizeCtl

put方法：
- 获取获取对应位置的node，tabAt函数
- 添加新的Node，使用casTab函数，casTab是compareAndSwapObject的封装
- 更新key对应的value或者处理hash冲突，使用synchronized，同时内部使用tabAt

注：https://juejin.im/post/6844903944800436238

总结：
- 用synchronized+ cas+node+nodetree代替segment，只有在hash冲突，或者修改已经存在的值时才加锁，锁的粒度小，降低锁竞争,减少阻塞；
- 链表节点数量大于8时，同时数组大小大于等于64时,会将链表转换为红黑树进行存储，查询时间复杂度从o（n）变成遍历红黑树o（logn）;