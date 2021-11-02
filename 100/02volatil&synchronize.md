# volatile
volatile是Java虚拟机提供的轻量级同步机制，有两个作用包括修饰的的共享变量对所有线程的可见性以及禁止指令重排序；
volatile无法保证共享变量的原子性，故称之为轻量级同步机制；

volatile是通过内存屏障实现可见性和禁止指令重排序。

内存屏障：

内存屏障又称内存栅栏，是CPU指令，作用包括保证特定操作的执行顺序和某些内存变量的内存可见性。

jmm内存模型实现了硬件底层的一系列内存屏障，屏蔽了不同硬件实现内存屏障的差异，统一提供4种内存屏障的机器码指令：

1. LoadLoad：读读屏障
2. StoreLoad：写读屏障
3. StoreStore：写写屏障
4. LoadStore：读写屏障

内存可见性：

mesi协议是通过定义并调整缓存的四种状态来保证内存可见性，通过总线嗅探机制监测缓存中数据是否发生变化并进行失效更新；

禁止指令重排：

指令重排序存在两次，第一次为字节码编译为机器码的阶段；第二次在CPU执行的时候会对指令进行重排；

1. 在每个volatile变量的写操作之前插入StoreStore屏障；
2. 在每个volatile变量的写操作之后插入StoreLoad屏障；
3. 在每个volatile变量的读操作后面插入LoadLoad屏障；
4. 在每个volatile变量的读操作之后插入LoadStore屏障；

https://www.cnblogs.com/ITPower/p/13580691.html

# synchronized
synchronized是java的内置锁，是一种对象锁，作用在对象之上，可重入互斥锁；

通过内部对象Monitor（监视器）实现，基于进入和退出Monitor对象实现方法和代码块的同步，Monitor底层依赖操作系统层面的mutex lock（互斥锁）实现，重量级锁，性能低；

java内置锁在1.5之后进行优化，提供了锁粗化，锁消除，偏向锁，轻量级锁等降低锁的开销，并发性能基本与Lock持平；

任何一个对象都有一个monitor与之关联，当一个monitor被持有后，它将处于锁定状态。

monitor：monitor是一个同步工具，也可以认为是一种同步机制，通常被描述为一个对象；所有的java对象天生是一个monitor；

Monitor由ObjectMonitor（c++）实现，ObjectMonitor内部存在两个队列_WaitSet与_EntrySet，用于保存ObjectWaiter对象列表（每个等待锁的的线程都会被封装为ObectWaiter），_Owner指向持有ObjectMonitor对象的线程。

多线程并发访问同步代码块：

1. 进入_EntrySet集合，线程获取到对象的monitor时，进入_Owner区域并把monitor中的owner变量设置为当前线程，monitor中计数器count+1；
2. 线程调用wait方法，将释放当前线程持有的monitor，owner值为null，count--，同时该线程进入_WaitSet等待唤醒；
3. 线程执行结果，释放持有的monitor并复位count值；

方法的同步是JVM通过ACC_SYNCHRONIZED标识符实现方法的同步，是隐式方式，无须通过字节码；

代码块同步通过monitorenter和monitorexit指令是jvm通过操作系统的互斥指令mutex实现；

## 对象内存布局
对象在内存中存储的布局分为三部分：对象头，实例数据，对其填充（padding保证为8字节倍数）

1. 对象头：
    1. mark word :hash码 or 偏向锁线程id，gc分代年龄（4位），偏向锁标识（1位），锁标志位（2位）；
    2. 类型指针（4位）
    3. 数组长度（如果是数组的话）
2. 实例数据：存放类的属性数据信息，包括父类的属性信息；
3. 对齐填充：非必须，jvm要求对象长度为8字节倍数；

锁标志位：
1. 01：无锁状态，对象头不存在monitor锁对象信息，存储对象的hashcode；
2. 01：偏向锁状态，锁标志位为1
3. 00：轻量级锁，对象头不包含monitor锁对象信息，指向偏向锁的线程id
4. 10：重量级锁，对象头存在monitor锁对象信息；
5. 11：GC


