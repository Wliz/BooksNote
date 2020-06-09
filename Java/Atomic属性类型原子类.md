## 属性类型原子类

类名|说明
-|-
|AtomicIntegerFieldUpdater | 原子更新整形的字段更新器|
| AtomicLongFieldUpdater | 原子更新长整形字段更新器 |
| AtomicReferenceFieldUpdater | 原子更新引用类型字段更新器 |

## AtomicReferenceFieldUpdater

类中引用类型属性字段原子操作类；

基于反射类，允许原子更新指定类的指定volatile字段，构造函数为protected，允许子类调用，其提供newUpdater方法用于创建抽象类实例;

使用限制：
- 字段为volatile修饰，保证线程之间共享变量立即可见
- 只能修饰实例变量，非类变量
- 修饰可改变量，非final变量

原理：
本质使用Unsafe对象属性相关API函数

主要方法：
```java
// 创建实例: tclass包含字段的对象类，vclass字段类，fieldName字段名字
newUpdater(Class<U> tclass,Class<W> vclass,String fieldName);
// 原子更新管理器指定对象的字段值为newValue，并返回旧值
public V getAndSet(T obj, V newValue);
// 比较对应类中字段的当前值是否为期望的，如果是期望值，则更新到新值并返回true，否则return false
public abstract boolean compareAndSet(T obj, V expect, V update);
// 获取管理更新器指定对象需要原子更新的字段值
public final V get(T obj);
// 更新管理器更新给定对象的指定字段为特殊值
public final void set(T obj, V newValue);
```
