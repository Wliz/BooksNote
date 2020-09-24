## 工作原理
1. 客户端发送请求到DispatcherServlet
2. DispatcherServlet查询HandlerMapping映射处理器
3. 处理器映射（根据xml，注解等）找到对应的处理器，生成处理器并返回给DispatcherServlet
4. DispatcherServlet调用HandlerAdapter处理适配器
5. HanlderAdapter经过适配调用具体处理（controller）
6. controller调用业务逻辑，并返回ModelAndView
7. HandlerAdapter将ModelAndView返回给DispatcherServlet
8. DispatcherServlet将ModelAndView传递给ViewReslover视图解析器
9. ViewReslover解析返回view
10. DispatcherServlet根据view视图渲染（将模型填充到视图中）
11. DispatcherServlet响应用户

## Spring MVC控制器是不是单例？若是，有什么问题，如何解决？

是单例模式，所以在多线程访问的时候如果有字段之类的会出现线程安全问题；

在控制器内部只做方法调用，不写字段；

## Spring的控制反转（IOC）？什么是依赖注入（DI）？

IOC：Inversion of Control

IOC是借助第三方实现具有依赖关系的对象之间的解耦；将传统意义上由程序代码直接操控的对象调用权交给容器，通过容器来实现对象组件的装配和管理；

把对象创建，初始化，销毁交给容器管理；

BeanFactory是IOC容器的核心，用来包装和管理各种bean；

主要实现：
- 依赖查找：容器提供回调接口和上下文组件
- 依赖注入（DI）

DI: Dependency Injection

依赖注入就是将实例对象传递到依赖对象中，包括构造器注入，setter注入，接口注入

## BeanFactory和ApplicationContext区别？

BeanFactory是bean集合的工厂，包含各种bean的定义，方便在接收到请求之后将对应的bean实例化；还包含bean生命周期的控制；

ApplicationContext是对BeanFactory的扩展，除了具有bean定义之外，还提供比如监听统一的资源文件读取方式（如ClassPathXmlApplicationContext, FileSystemXmlApplicationContext等），国际化支持，已在监听器中注册的bean的事件；


## spring开发配置有几种？
- xml文件配置：使用spring命名空间支持的标签（如aop，tx，bean，jdbc等）实现
- 注解配置：spring2.5之后开始支持，需要在配置文件中配置注解装配
- java配置：使用@Configuration和@Bean注解实现；Bean标记表示一个对象，@Configuration表示当前类是作为bean定义的资源

## Spring中Bean的生命周期

指在一个Bean被实例被初始化后，需要执行一系列的初始化操作才能达到可用状态；同样在Bean实例不被调用时需要进行相关的析构操作，并从Bean容器中移除；

Spring BeanFactory负责管理在Spring容器中创建的Bean实例的生命周期；Bean的生命周期由两组回调组成：
- 初始化之后的回调
- 销毁之前的回调

Sring提供四种方式管理Bean的生命周期事件：
- InitializingBean和DispoableBean回调接口
- 针对特殊行为的Aware接口
- Bean配置文件中的init和destroy方法
- @PostConstructor和@PreDestroy方法

## Srping Bean的作用域之间有什么区别？

Bean有以下5个作用域：
- singleton: 默认作用域，不管接受多少个请求，一个容器只能有一个Bean实例，单例模式由BeanFactory自身维护
- prototype: 与单例模式相反，为每一个Bean请求创建一个实例
- request: 在请求范围内为每一个来自客户端的网络请求创建一个实例，请求完成后，bean失效并被垃圾回收
- session：和request类似，确保每个session中有一个Bean实例，session过期后，bean失效
- global-session

## Spring中的单例Bean线程安全吗？

spring框架并没有对单例Bean做多线程封装，单例bean的线程安全和并发访问需要开发者去处理；

实际上，Spring Bean并没有可变的状态，所以在某种程度上是线程安全的，如果bean有多重状态，则使用prototype多态模式；

## Spring中有哪些不同类型事件？

- 上下文更新时间（ContextRefreshEvent）：在ApplicationContext初始化或更新时发布，也可以在ConfigurableApplicationContext接口中的refresh()方法触发；
- 上下文开始事件（ContextStartedEnvent）：当容器调用ConfigurableApplicationContext的start方法时触发
- 上下文停止事件（ContextStoppedEvent）:调用stop方法
- 上下文关闭事件（ContextClosedEvent）：当ApplicationContext被关闭时触发，其所管理的单例Bean都被销毁
- 请求处理事件（RequestHandledEvent）:当web的一个请求结束时触发

## Spring用到的设计模式？
 
-  代理模式：AOP和remoting中用的多
- 单例模式：BeanFactory创建实例对象
- 模板方法：jdbcTemplate，RedisTemplate等
- 工厂模式：BeanFactory创建实例对象

## Spring容器加载方式
- 类路径：ClassPathXmlApplicationContext
- 绝对路径：FileSystemApplicationContext
- 无配置文件加载（注解）：AnnotationConfigApplicationContext
- springboot: EnablededWebApplicationContext

Spring 容器启动的核心方法

AbstractRefreshableApplicationContext:
- refresh()
    - loadBeanDefinitions(beanFactory)
        - XmlBeanDefinitionReader解析xml配置文件
        - loadBeanDefinitions(beanDefinitionReader)
            - getConfigLocations()获取配置文件地址
            - read.loadBeanDefinitions(configLocations)
                - AbstractBeanDefinitionReader(抽象父类)

XmlBeanDefinitionReader: xml文件中Bean定义解析器
- loadBeanDefinitions(configLocations)   
    - 文件转换为InputStream
    - doLoadBeanDefinitions(inputSource, source): 加载BeanDefinition
        - doLoadDocument（inputSource, resource）:解析文件转换为Document对象实例
        - registerBeanDefinitions(doc, resource): 注册BeanDefinition
            - BeanDefinitionDocumentReader:解析Document对象(委托)
                - documentReader.registerBeanDefinitions(doc, createReaderContext(resource)): 注册BeanDefinition

DefaultBeanDefinitionDocumentReader: 最终解析
- parseDefaultElement(ele, delegate):解析import, alias, bean, beans
    - BeanDefinitionHolder: 钩子
    - BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder getReaderContext().getRegistry())
        - DefaultListableBeanfactory:完成注册


## BeanDefinition

Spring要根据BeanDefinition来实例化Bean，把解析的标签，扫描的注解类封装成Beandefinition；

实现类：
- ChildBeanDefinition: 可以继承父类设置，对RootBeanDefinition有一定的依赖关系
- GenericBeanDefinition: 支持动态定义父类依赖
- RootBeanDefinition： 表示是一个可合并的BeanDefinition

通常GenericBeanDefinition用来注册用户可见的BeanDefinition；Child/RootBeanDefinition用来预定义具有父子关系的BeanDefinition；

http://www.spring4all.com/article/16302

## Spring Bean的实例化以及di过程？？？？？todo
  

## AOP 面向切面编程

通过扫描注解@EnableAspectJAutoProxy注册AOP入口类；

当参数proxyTargetClass有值时：

false:(默认)
1. 目标类实现接口，则使用JDK动态代理
2. 目标对象没有实现接口，使用CGLIB代理

true:
1. 目标对象实现接口，使用CGLIB代理
2. 目标对象没有实现接口（只有实现类），使用CGLIB代理

exposeProxy: 是否需要吧代理对象放入到ThreadLocal中;

jdk代理：JdkDynamicAopProxy

CGLIB代理：CglibAopProxy

寻找所有切面：
拿到所有的BeanDefinition对应name，拿到对应Class，判断该类上是否有@Aspect注解的类，是的话就是切面类；循环该类内部除了@PointCut注解之外的方法，找到对应的Around，Before等之类注解，把注解中信息如表达式等信息封装成对象AspectJAnnotation，然后创建PointCut对象，将注解中表达式设置到PointCut对象中，然后创建Advice对象（根据注解不同，创建对应实例），并最终把Advice和PointCut封装Advisor对象；

创建代理过程：
- 创建代理工厂对象ProxyFactory【AbstractAutoProxyCreator.createProxy】
- 切面对象包装，将自定义的MethodInterceptor【Object[] specificInterceptors】类型的类包装成Advisor切面类并加入代理工厂
- 根据proxyTargetClass配置判断使用jdk动态代理还是cglib动态代理【DefaultAopProxyFactory.createAopPoxy(config)】
- 创建代理对象，并把代理工厂对象传入到jdk或cglib中

## 嵌套AOP

## Spring MVC

Spring MVC是基于Servlet规范来完成一个请求响应模块，采用约定大于配置方式；

取代web.xml配置文件，采用的是spi（Service Provider Interface）规范加载META-INF下的services/ServletContainerInitializer文件