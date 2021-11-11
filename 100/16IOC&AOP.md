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

# 代理
Java中代理大致分为静态代理与动态代理两类,而动态代理又可以分为JDK动态代理与CGLIB动态代理,同时也有与之对应的设计模式代理模式.

代理模式: 为其他对象提供一种代理以控制对这个对象的访问(操作被访问者，通过代理对象访问对象，而非直接访问真实对象）;

代理是通过对代理对象进行包装增强,避免对真实对象的直接访问,以达到对真实对象的功能增强和简化访问的能力;

## 静态代理

静态代理需要代理对象与目标对象实现相同的接口,然后代理对象实现对目标对象的代理;

优点:

- 可以在不修改目标对象的前提下实现对目标对象的增强;

缺点:

- 由于代理对象与目标对象需要实现相同接口,会产生过多的代理类;
- 一旦接口增加方法,则代理对象与目标对象都需要进行修改,不易维护;

因为静态代理弊大于利,所以引出后续的动态代理.

```Java
// interface
public interface StaticInterface {

  /**
   * 代理方法
   *
   * @return 字符串
   */
  String someMethod();
}

// targetObject目标对象
public class StaticClass implements StaticInterface {

  /**
   * 代理方法
   *
   * @return 字符串
   */
  @Override
  public String someMethod() {
    System.out.println("目标类方法");
    return "some method proxyed";
  }
}
// proxyObject代理对象
public class StaticProxy implements StaticInterface {

  // 目标类
  private StaticInterface target;

  public StaticProxy(StaticInterface target) {
    this.target = target;
  }

  /**
   * 代理方法
   *
   * @return 字符串
   */
  @Override
  public String someMethod() {
    String result = target.someMethod();
    System.out.println("post process");
    return result;
  }
}
```

## 动态代理

相对于静态代理来说,静态是在编译前手动编写代理对象,而动态代理是在内存中动态的构建代理对象,不需要提前编写;根据其实现技术不同,可分为JDK动态代理与CGLIB动态代理;

### JDK动态代理
JDK动态代理的技术本质是通过反射实现,在程序运行过程中动态构建实现相同接口的代理对象;

相比静态带来说,有以下区别:
- 静态代理在编译时实现,JDK动态代理在运行时实现对目标对象的包裹增强;
- JDK动态代理代理对象不需要实现接口,只需要目标对象实现接口即可;

缺点:
- 代理目标对象必须实现接口,否则无法代理;
- 使用反射方式实现,效率相比CGLIB低;

注:
JDK动态代理需要用到Java中的Proxy类实现以及InvocationHandler;

```Java
// interface
public interface DynamicInterface {
  String someMethod();
}
// targetObject目标对象
public class TargetObject implements DynamicInterface {

  @Override
  public String someMethod() {
    System.out.println("Dynamic target class");
    return "target class";
  }
}
// 动态生成代理对象
Proxy.newProxyInstance(DynamicClass.class.getClassLoader(), DynamicClass.class.getInterfaces(), new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          // 代理逻辑xxxx
        return null;
      }
    })
```

### CGLIB动态代理
CGLIB(Code Generation Library代码生成库)是一个第三方代码生成库,运行时在内存中动态构建一个目标对象的子类从而实现对目标对象的功能代理增强;

CGLIB底层技术为字节码框架ASM,通过转换字节码生成子类,效率较好;

与JDK动态代理区别:
- JDK动态代理需要目标对象实现接口,而CGLIB无须目标对象实现接口;
- 实现技术不同,前者使用反射技术在运行时构建实现相同接口代理对象,效率低;后者使用字节码技术生成目标对象子类,效率高;

注: 字节码技术ASM了解即可,无须深入;

总结:
- 静态代理实现简单,只需要代理对象对目标对象进行包装,即可实现功能增强和访问控制,在编译之前实现;会导致大量代理类,提高维护难度;
- 静态代理类在编译之前实现,编译为class文件,运行时直接运行,效率高;
- JDK动态代理通过反射实现,但要求目标对象实现接口,比较消耗性能,但解决了静态代理大量代理类缺点(变编译前实现为运行时解析实现);
- CGLIB动态代理通过字节码技术生成目标对象子类,通过字节码直接替换,效率比JDK动态代理高,但目标对象不能为final类型(final无法继承);
```Java
// targetObject 目标对象
public class CglibTarget {
  public String someMethod() {
    String message = "======cglib class";
    System.out.println(message);
    return message;
  }
}
// 代理处理类(动态生成代理对象)
public class CglibInterceptor implements MethodInterceptor {

  @Override
  public Object intercept(
      Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
    System.out.println(">>>>>> method interceptor start");
    Object result = methodProxy.invokeSuper(o, objects);
    System.out.println(">>>>>> method interceptor end ...");
    return result;
  }
}
// Test 测试
import org.springframework.cglib.proxy.Enhancer;
public class CglibTest {

  public static void main(String[] args) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(CglibTarget.class);
    enhancer.setCallback(new CglibInterceptor());
    CglibTarget o = (CglibTarget) enhancer.create();
    String s = o.someMethod();
    System.out.println(s);
  }
}
```

资料:
- https://segmentfault.com/a/1190000011291179#:~:text=%E5%8A%A8%E6%80%81%E4%BB%A3%E7%90%86%E5%8F%88%E8%A2%AB%E7%A7%B0,%E5%B9%B6%E5%8A%A0%E8%BD%BD%E5%88%B0JVM%E4%B8%AD
