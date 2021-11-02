JUC（java.util.concurrent）并发包下存在一些并发工具，如CountDownLatch, CyclicBarrier, Seamphore；以及相关的Concurrent类如ConcurrentHashMap等线程安全的动态数组，以及对应的Executor线程池框架，创建不同类型的线程池；
# CountDownLatch(计数栅栏)
含义：允许一个或多个线程等待某些操作完成之后（到达某个状态即栅栏）再执行后续内容；

作用场景：模拟多线程并发

特点：
1. 业务线程调用await方法阻塞等待；
2. 控制线程（非统一业务执行）调用countDown计数减一操作，当减到0时所有阻塞线程同时运行；
3. 无法重置复用；（使用一次即作废）

```Java
// 初始化计数器数值
CountDownLatch countDownLatch = new CountDownLatch(6);

// ……

```
# Seamphore（信号量）
含义：信号量控制，用于控制同时访问共享变量的线程个数；

特点：acquire获取许可，release释放许可；
# CyclicBarrier(循环栅栏)
含义：一组线程全部执行到某个状态后即栅栏才同时向后执行；

特点：
1. 业务线程调用await方法阻塞等待一组线程到达栅栏；
2. 非业务线程调用reset可以重置循环栅栏，可复用；
3. 业务线程到达栅栏后可设置回调函数；

# CountDownLatch与CyclicBarrier异同
1. CountDownLatch业务线程到达状态栅栏数量为一个或者多个，可由非业务线程控制计数器减一；且不可复用；
2. CyclicBarrier业务线程到达状态栅栏为一组线程，与初始化数量一直；可由非业务线程控制复用调用，且可设置回调函数；
# Lock(锁)
Lock为锁接口，实现该接口的锁具有不同功能，但都是基于AQS（抽象队列同步器）原理实现；其核心方法为lock，unlock， trylock；

从公平性划分：分为公平锁，非公平锁；
1. 公平锁：将申请锁线程放入队列，按照FIFO顺序排队获取锁，不会有线程饿死；
2. 非公平锁：线程申请锁时，先插队获取锁，若获取锁成功，则线程执行，否则将该线程加入等待队列中；【可以减少线程唤醒的上下文切换性能开销，这也是非公平锁性能高于公平锁原因】

注：公平锁与非公平锁都是要加入到等待队列中，只是公平锁在获取锁时先插队，而非非公平锁的的先尝试获取锁；插入队列前会再次判断当前线程是否有获取锁资格，否则入队挂起等待唤醒；

一朝排队，永远排队【公平锁正序】

从读写能力上划分：读锁，写锁，读写锁；
1. 读写互斥：读排斥写，写排斥读；

注：Lock锁常用的工具类LockSupport，对应方法为par，unpark；

从锁的实现方式划分：乐观锁，悲观锁：

1. 乐观锁：乐观锁基于CAS（Compare and Swap比较交换），涉及内存值，期望值，新值，当内存值与期望值相同时设置新值；【cas存在ABA，自旋性能损耗，只能保证一个变量的三个问题】
2. 悲观锁：每次使用都要获取锁，线程独占使用，独占共享对象；

# Lock常用实现类？
ReentrantLock（可重入锁），ReentrantReadWriteLock(可重入读写锁)，ConcurrentHashMap中的Segment分段锁等；
## ReentrantLock(可重入锁)

存在内部类NonfairSync（非公平同步器）, FairSync（公平同步器），支持公平与非公平可重入锁；
```Java
/**
     * Sync object for non-fair locks
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
        final void lock() {
            // 非公平第一次先尝试获取锁，成功则获锁成功
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
            //            final boolean nonfairTryAcquire(int acquires) {
//                final Thread current = Thread.currentThread();
//                int c = getState();
//                if (c == 0) {
//                    if (compareAndSetState(0, acquires)) {
//                        setExclusiveOwnerThread(current);
//                        return true;
//                    }
//                }
//                else if (current == getExclusiveOwnerThread()) {
//                    int nextc = c + acquires;
//                    if (nextc < 0) // overflow
//                        throw new Error("Maximum lock count exceeded");
//                    setState(nextc);
//                    return true;
//                }
//                return false;
//            }
        }
    }

    /**
     * Sync object for fair locks
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            // 插入队列前先去判断当前线程是否有资格获锁，有的话直接获取锁，不必入队列
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            // 当前线程已获取锁，重入
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                    // state + 1；
                setState(nextc);
                return true;
            }
            return false;
        }
    }
```
