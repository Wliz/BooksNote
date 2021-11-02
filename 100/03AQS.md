# AQS 原理
AbstractQueuedSynchronizer：抽象队列同步器，是一种构建锁和同步器的框架，Lock包下锁实现类以及Seamphore, CountDownLatch都是基于AQS框架；AQS内部存在多个等待队列（与Condition有关）和一个同步队列（与内部类Node有关）；

Node类是访问同步代码的线程的封装，包括线程本身和对应的等待状态waitStatus;内部存在两个常量SHARED（共享）与EXCLUSIVE（独占）模式；

解析：
1. 内部定义变量：volatile int state(the synchronization)，来标识当前线程的同步状态；若state=0，表示没有线程持有锁，线程通过CAS操作将state置为1；若state不为0，表示有线程持有锁，则其他线程加入同步队列等待唤醒竞争锁；
2. 内部类Node构建一个双向链表的同步队列，用于竞争锁的线程排队工作，有prev，next前驱后继指针，方便线程执行结束后快速唤醒下一个等待线程；
3. 通过ConditionObject内部类构建多个等待队列，当Condition调用wait方法后，将该线程加入等待队列中，当调用signal时，将等待队列的线程移动到同步队列参与锁的竞争；

注：
1. 当竞争锁失败，加入同步队列时，调用的位LockSupport.park；当唤醒线程时调用的是LockSupport.unpark;
2. 线程等待状态包括（CANCEL=1取消；SINGLE=-1阻塞；CONDITION=-2等待唤醒；PROPAGATE=-3传播）；

使用：
1. 独占模式：
    a. 重写tryAcquire，获取锁；
    b. 重写tryRelease，释放锁；
2. 共享模式：
    a. 重写tryAcquireShared，获取共享锁；
    b. 重写tryReleaseShared，释放共享锁；

 执行过程：
 加锁：acquire(int arg) -> tryAcquire(arg)[自定义重写] -> addWaiter(Node.EXCLUSIVE) [增加到同步队列尾部并返回] -> [获锁失败]acquireQueued(final Node node, int arg)[节点阻塞，是否需要中断] -> selfInterrupt()[中断当前线程]
 释放锁：release(int arg) -> tryRelease(arg)[自定义重写] -> unparkSuccessor(h)[唤醒同步队列等待节点]

 参考资料：https://juejin.im/post/6844903938202796045