# IOC(控制反转Invese Of Control)

将Bean对象的创建,对象之间关联关系的维护由原来通过编码方式维护,通过Spring容器实现对象的装配与管理;

作用: 解决了原来上层建筑依赖下层建筑的问题,实现了上层建筑对下层建筑的控制;

实现原理: 反射 + 容器 + 设计模式;

DI(Dependency Injection)依赖注入,是IOC的一个特殊实现,完成对象之间的关联注入;(DI是通过反射实现)
- 构造器注入: 通过有参构造注入
- setter注入:先无参实例化,再通过setter注入

spring提供两种不同类型的IOC容器: BeanFactory, ApplicationContext; 是一个高度可扩展的无侵入性(应用程序的组件无须实现Spring特定接口)容器;

Spring容器可以通过xml配置,也可以通过注解实现IOC,初始化时预先读取配置文件,根据配置文件或元数据创建与组织对象存入容器内,程序使用的时候再从容器内取出;

如ListableBeanFactory意味着实现了此接口的工厂可以遍历它所有的Bean，HierarchicalBeanFactory代表实现它的工厂是存在继承关系的，即可能有Parent Factory，AutowireCapableBeanFactory代表它具备自动装配Bean的功能

## ObjectFactory, FactoryBean, BeanFactory有什么区别?
有名字可知分别为:对象工厂,工厂Bean, Bean工厂,均提供依赖查找能力;

- ObjectFactory提供延迟依赖查找,当需要获取某类型的Bean时,调用getObject方法才能查找到目标Bean对象;是一个对象工厂,当需要对象时,通过方法创建一个对象;
- FactoryBean不提供延迟性,在被依赖注入或依赖查找时,得到的就是通过getObject方法获取的实际对象.FactoryBean关联着某个Bean,可以说spring中它就是某个Bean对象,无需通过getObject方法查找;如果想要获取FactoryBean本身,在beanName前添加&即可;
- BeanFactory是Spring底层核心IOC容器,内部保存所有单例Bean;ObjectFactory与FactoryBean自身不具备依赖查找能力,都是通过BeanFactory提供;

# AOP(Aspect Oriented Programing)面向切面编程

AOP是通过动态代理方式分离业务逻辑与系统服务,对业务进行增强;

作用: 解决了关注点分离,让系统架构变得高内聚低耦合;

实现原理: 动态代理(jdk动态代理 + cglib动态代理)

场景: 声明式事务,通用日志,通用缓存,全局异常处理等;



资料:
- https://www.jb51.net/article/224617.htm
- https://www.cnblogs.com/lifullmoon/p/14422101.html
- 重点[有助于理解spring]: https://juejin.cn/post/6933745299932315655