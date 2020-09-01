# 对象创建过程
- 检查加载
- 分配内存：指针碰撞（连续规整空间分配），空闲列表（GC散乱回收）【并发安全：CAS本地重试，本地线程分配缓冲】
- 内存空间初始化：参数实例默认值
- 设置：元数据信息，如对象hash码，以及GC分代年龄等信息
- 对象初始化：按照代码初始化

注：（8个字节的倍数）对象的内存布局（对象头，实例数据，【数组长度（数组对象才有）】，对齐填充）

## 对象包括哪些内容？只能是8byte的倍数


对象头表（64bit）：
lock【锁标记】：
- 01：默认，偏向锁
- 00： 轻量级锁
- 10： 重量级锁
- 11： GC标记

biased_lock(是否偏向偏向锁标记)：
- 0：不可偏向
- 1：可偏向

age：4bit的对象年龄（对应年轻代中的Survivor中的from和to区，复制一次加1）

identity_hashcode:32位的对象标识Hash码，采用延迟加载技术；调用System.identityHashCode计算；当对象被锁定时，该值会移动到管程monitor

thread：持有偏向锁的线程id

epoch： 偏向时间戳

ptr_to_lock_record: 指向栈中锁记录的指针

ptr_to_heavyweight_monitor: 指向管程Monitr的指针

| 类型 | Mark Word(64bit)对象头 | kclass point(32bit 4byte) 类型指针 |
|-|-|-|
| 无锁 | unused:25 - identity_hash_code:31 - unused:1 - age:4 - biased_lock:1 - lock:2 | 4|
| 偏向锁 | thread:54 - epoch:2 - unused:1 - age:4 - biased_lock:1 - lock:2 | 4 | 
| 轻量级锁 | ptr_to_lock_record:62 | lock:2 | 4 |
| 重量级锁 | ptr_to_heavyweight_monitor:62 | lock:2 | 4 |
| GC标记 | 无 | lock:2 | 4 |

注：当无锁时分为两种情况如下：

1. 无锁不可偏向：001，有hashcode
2. 无锁可偏向：101，无hashcode

另外锁膨胀其实就是锁升级过程：偏向锁 -》 轻量级锁 -》重量级锁；

</br>

- 对象头
    - Mark Word：64bit
    - 类型指针：kclass point：4byte或8byte
    - 数组长度【数组对象才有】
- 实例数据
- 对其填充字节    

https://blog.csdn.net/lkforce/article/details/81128115


包括两部分mark word【64bit = 8byte】和kclass point【32bit = 4byte，长度有可能为8，要看有没有开启指针压缩】（类型指针）

JVM启动时会进行一系列复杂活动，如加载配置，系统类初始化等；在这个过程中会使用大量的synchronized对对象加锁，而且这些锁不是偏向锁；为了减少初始化时间，jvm默认延迟加载偏向锁，延迟大概4s左右，可以通过jvm参数-XX:BiasedLockingStartupDelay=0取消延迟加载；

偏向锁是一个特殊状态的无锁（对应thread和epoch均为0）；

https://www.cnblogs.com/LemonFive/p/11246086.html

小端模式：高地址存高字节，低地址存低字节

synchronized如果是同一个线程加锁，偏向锁；
交替执行，轻量级锁；
资源竞争--mutex，重量级锁；



# Java字节码指令
Java虚拟机是基于栈的架构，其指令有操作码和操作数组成。
## 指令
### 栈操作指令
- load:用于将局部变量表的指定位置的响应类型变量加载到栈顶；（iload, lload, fload, dload, aload[ref类型变量进栈]）
- store: 用于将栈顶的相应类型数据保存到局部变量表的指定位置
- const、push：将相应类型的常量放入栈顶；（aconst_null[null进栈]，iconst_m1[int型常量-1进栈]，lconst_x, fconst_x, dconst_x,bipush[byte型常量进栈],sipush[short类型常量进栈]）
- ldc:从常量池中取出常量放入栈顶

注：当const将常量加载到操作栈顶时，如果超过5，使用指令bipush；

| 常量池操作 | 含义 |
|---|---|
| ldc | int，float，String型常量从常量池推送至栈顶|
| ldc_w | int，float，String型常量从常量池推送至栈顶（宽索引）|
| ldc2_w | long，double型常量从常量池推送至栈顶（宽索引）|
- pop：栈顶数值出栈操作
- dup：赋值栈顶的指定个数的数值，并将其压入栈顶指定次数
- swap：栈顶的两个数值互换，且不能是long/double
### 字段调用
- getstatic: 获取类的静态字段，将其值压入栈顶
- putstatic：给类的静态字段赋值
- getfield: 获取对象的字段，将其值压入栈顶
- putfield: 给对象字段赋值
### 方法调用
- invokevirtual：调用实例方法
- invokestatic：调用类方法
- invokeinterface：调用接口方法
- invokespecial：调用特殊实例方法
- invokedynamic：由用户引导方法决定
### 方法返回
- ireturn: return int value
- lreturn: return long value
- freturn: return float value
- dreturn: return double value
- areturn: return ref value
### 对象和数组
- new：创建类实例
- newarray，anewarray，multianewarray: 创建数组
- xload（x）：数组加载到操作数栈（x为b,c,s,i,f,d,a）
- arraylength: 数组长度
- instanceof，checkcast：类实例类型
### 运算指令

| 运算 | int | long | float | double |
|---|---|---|---|---|
| 加法 | iadd | ladd | fadd | dadd |
| 减法 | isub | lsub | fsub | dsub |
| 乘法 | imul | lmul | fmul | dmul |
| 除法 | idiv | ldiv | fdiv | ddiv |
| 求余 | irem | lrem | frem | drem |
| 取反 | ineg | lneg | fneg | dneg |
| 按位或 | ior | lor |
| 按位与 | iand | land |
| 自增 | iin |
### 类型转换
- 宽化类型小范围-》大范围：int,long, float ,double
- 窄化类型，显示调用指令（虚拟机不会抛出异常）如i2b,i2c, f2i
### 流程控制
- 条件分支：ifeq,iflt, ifnull, ifnonnull
- 复合分支：tableswitch，lookupswitch
- 无条件分支：goto， goto_w, jsr,  jsr_w, ret
### 同步与异常
- 异常指令：athrow，虚拟机中处理异常采用异常表完成（对应代码的catch）
- 同步：方法级同步和方法内部分代码同步，依靠管程（Monitor）实现；monitorenter，monitorexit