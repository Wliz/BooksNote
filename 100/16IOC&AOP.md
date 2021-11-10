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

资料:
- https://www.jb51.net/article/224617.htm
- https://www.cnblogs.com/lifullmoon/p/14422101.html
- 重点[有助于理解spring]: https://juejin.cn/post/6933745299932315655

# AOP(Aspect Oriented Programing)面向切面编程

AOP是通过动态代理方式分离业务逻辑与系统服务,对业务进行增强;

作用: 解决了关注点分离,让系统架构变得高内聚低耦合;

实现原理: 动态代理(jdk动态代理 + cglib动态代理)

场景: 声明式事务,通用日志,通用缓存,全局异常处理等;

AOP的实现分为SpringAOP实现与AspectJ实现,分别为动态织入与静态织入;

AspectJ静态织入分为三种:
- 编译时织入: 在代码编译时,将切面代码融入进去,生成具有完整功能的Java字节码,但需要特殊的Java编译器
- 编译后织入: 这就是所熟悉的二进制织入。它被用来编织现有的类文件和JAR文件与我们的切面
- 加载时织入:在Java字节码加载时,将切面代码融入进去,需要特殊的类加载器

Spring AOP使用动态织入,又称运行时织入,使用动态代理实现:(有一定的性能开销)
- jdk动态带来: 代理类需要实现接口,生成一个实现相同接口的代理类;
- cglib动态代理: 代理类不需要实现接口,使用ASM字节码技术生成代理类子类实现;

注: spring在未强制指定cglib代理时,优先使用jdk动态代理(实现接口),强制指定时proxy-target-class,一定使用cglib动态代理;

相关概念定义:

- 切面(Aspect): Aspect类似Java中类声明,在Aspect中包含一些Pointcut以及相应的Advice(增强);
- 连接点(Join Point): 表示程序中明确定义的点,典型的包括方法调用,对类成员的访问,以及异常处理块的执行等,它还可以嵌套其他的join point;
- 切点(Pointcut): 表示一组连接点(Join point),这些连接点或是通过逻辑关系组合起来,或是通过正则表达式集中起来,定义了相应的Advice(增强)将要发生的地方;
- 增强(Advice): 定义了在Pointcut定义的地方要执行的操作,通过before, after,around来区别是在切点前与后执行相应的代码;
- 目标对象(Target): 织入Advice的目标对象;
- 织入(Weaving): 将Aspect与其他对象链接起来,并创建增强对象的过程;

以上概念之间的关系可以理解为连接点(Join Point)是所有方法的的执行点,而Pointcut是一组描述信息规则,用于筛选特定的Join Point;之后通过织入将符合Pointcut的Join Point织入Advice;而Aspect就是Pointcut与Advice的组合体;

Join Point类型:
- 构造方法调用
- 字段的设置与获取
- 方法调用
- 方法执行
- 异常处理
- 类初始化

Advice:

- Before前置通知: 在目标方法执行前调用通知功能(增强Advice);
- After后置: 在目标方法执行之后调用通知功能(增强Advice)
- After-returning返回通知: 在目标方法成功执行之后调用通知(增强Advice);
- After-throwing异常通知: 在目标方法抛出异常后调用通知;
- Around环绕通知: 在目标方法执行之前,之后调用通知;

spring对AOP的支持有两种:

- Spring AOP: 通过动态代理(jdk+cglib),只支持方法级别的连接点Join Point;通过在代理类内部包裹切面,在运行期将切面织入到spring管理的bean中;
- 注入式AspectJ切面: 提供构造器,字段级别的连接点;

资料:
- https://zhuanlan.zhihu.com/p/161705262
