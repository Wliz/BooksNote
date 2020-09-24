# HashMap 源码分析

HashMap实现Map接口，允许null键和值；非同步，不保证有序，也不保证顺序不随时间变化；

HashMap中以Node数组实现的哈希桶数组，用key的哈希值与数组下标最大值(length - 1)得到元素对应数组下标；

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

```

重要参数：

- DEFAULT_INITIAL_CAPACITY = 1 << 4 : 位运算默认初始值大小16
- MAXIMUM_CAPACITY = 1 << 30 : 最大容量
- DEFAULT_LOAD_FACTOR = 0.75f : 默认负载因子
- TREEIFY_THRESHOLD = 8 : 超过该值时bucket转红黑树【同时要求数组长度也要大于等于64】
- UNTREEIFY_THRESHOLD = 6 : bucket取消树化的阈值

源码注释中强调如果对迭代性能要求高，则不要将capacity设置过大，load factor设置过小；当bucket中entries数目大于capacity * load factor时需要调整bucket大小为当前的两倍。

## put分析

处理过程：

    1.对key做hash，然后计算对应数组下标index元素
    2.如果对应index没有hash冲突，则直接放入对应的bucket中（第一个元素）
    3.如果hash冲突（碰撞），以链表形式存储bucket中
    4.如果链表过长（binCount >= TREEIFY_THRESHOLD - 1），同时还要判断集合长度是否小于64，若小于则扩容，否则转换链表为红黑树
    5.最后一系列处理后，判断节点是否存在（不管是否是新节点还是旧节点），用value替换并返回
    6.++size > threshold 超了，则重置哈希表

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

CurrentHashMap在1.7的时候采用分段锁实现并发；而1.8使用cas+synchronized保证并发，内部仍然存在Segment，为了保证序列化的兼容性；

Node<k,v>[] table;

Node<k, v>[] nextTable;

int sizeCtl;

table默认大小为16，存储node节点，扩容时扩大两倍； Node中key和hash不可变，value和next是volatile，保证线程可见性；

nextTable默认null， 扩容时新生成的数组，大小为原来的2倍；

sizeCtl默认0，用来控制table的初始化和扩容操作 ，-1表示初始化或resized，-（1+正在重置线程数）正在被多个线程扩容；

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
- 用synchronized+ cas+node+nodetree代替segment，只有在hash冲突，或者修改已经存在的值时才加锁，锁的粒度小，减少阻塞；
- 链表节点数量大于8时，会将量表转换Wie红黑树进行存储，查询时间复杂度从o（n）变成遍历红黑树o（logn）