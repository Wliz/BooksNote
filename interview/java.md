# Java 
## 线程
- 线程A执行线程B的join方法，A必须等待B执行完毕后，A才能继续自己的工作；调用join方法使得线程出让执行权，等待自己执行完毕
- sleep，join方法调用后，持有的锁不会被释放
- wait方法前，必须持有锁，调用之后，锁被释放;wait方法返回后，锁被重新持有（notify唤醒）
- notify调用不会释放锁,调用前需要持有锁（尽量使用notifiAll,否则可能发生信号丢失）
- 多个线程同时拥有一个对象锁时，按照先后顺序串行的，必须等待一个线程方法执行完毕执行另外线程方法
- 等待/通知范式：
    
        等待方：
            1、获取对象锁
            2、循环判断条件是否满足，不满足wait
            3、满足条件执行后续业务逻辑
        通知方：
            1、获取对象锁
            2、改变条件
            3、唤醒所有等待对象的线程
    等待超时：
    ```Java
    long overtime = t + now;
    long remainTime = t;
    while(不符合条件 && remainTime > 0) {
        wait(remainTime);
        remainTime = overtime - now;
    }
    ```
