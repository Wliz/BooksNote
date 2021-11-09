# Spring框架

Spring框架时为了解决软件开发的复杂性而创建的;之前使用EJB开发Java引用,存在学习和应用成本过高,开发过程存在大量重复代码,配置复杂等等问题;

Spring 初衷:
- 使Java相关开发更简单;
- 面向接口编程而非使用类;[定义接口(标准)];
- 为JavaBean提供更好的应用配置框架;
- 更多强调面向对象设计;
- 尽量减少必须要的异常捕捉(Spring实现基于AOP的全局异常捕捉);
- 使应用程序更容易测试;

Spring目标:
- 方便使用spring;
- 应用不依赖于Spring APIs;
- Spring不和现有方案竞争,而是将不同方案融合;

Spring是一个轻量级的控制反转(IOC)和面向切面(AOP)容器框架.
- 轻量: 从大小和开销两方面Spring均为轻量级,可以在大小1MB的jar文件发布,所需的处理开销小;同时Spring非侵入性,应用中对象不依赖Spring特定类;
- 控制反转: 一个对象依赖的其他对象是通过容器主动装配完成,而非传统的自己创建或查找依赖对象;
- 面向切面: 分离业务逻辑与系统级服务(如日志,事务等)进行内聚性开发;应用开发只需关注业务逻辑即可;
- 容器: Spring包含并管理应用对象的配置和生命周期,可以配置每个bean如何创建(基于可配置原型prototype),可以创建一个单独的实例或者每次都创建一个新的实例;
- 框架: Spring整合不同组件,构成复杂应用;

|--|--|--|--|
|-|-|-|-|
|Data Access/Integration| Data Access/Integration| web| web |
|JDBC|ORM|Websocket|Servlet|
| OXM | JMS | Web | Portlet |
| Transactions|-|-|-|
|-|-|-|-|
|AOP| Aspects |Instrumentation | Messaging |
|-|Core Container | -|-|
| Beans | Core | Context | SpEL |
| -|  - | - | -|
| Test |

# 整体架构

- core层 -- Core Container: 基础设施层,核心功能Bean-Context管理,是Spring项目的基础依赖
    - spring-core: Spring核心包,包含IOC和DI;
    - spring-beans: Bean包,核心是BeanFactory,通过IOC将Bean依赖与配置分离,实现Bean的动态注入;
    - spring-context: 相当于注册表,维护Bean上下文信息,理解为Spring的IOC容器;
    ```Java
    // ApplicationContext是上下文基础入口
    public interface ApplicationContext extends 
        EnvironmentCapable,        //读取环境信息
        ListableBeanFactory,       //读取当前容器Bean信息
        HierarchicalBeanFactory,   //读取父容器的Bean
        MessageSource,             //国际化接口
        ApplicationEventPublisher, //事件发布接口
        ResourcePatternResolver    //资源解析
    {
            @Nullable
            String getId();

            String getApplicationName();

            String getDisplayName();

            long getStartupDate();

            @Nullable
            ApplicationContext getParent();

            AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;
    }

    // spring-context-support提供将第三方库集成到spring-context中,包括缓存,邮件,定时调度,模板引擎;[源码中可查询目录]
    ```
    - spring-expression: EL表达式支持,运行时灵活查询和操作对象;
- ext层 -- 拓展层(较为核心),提供额外的支持,按需引入
    - spring-aop: 基于动态代理(jdk动态代理or cglib代理)的切面编程,通过定义切入点,切面,进行切面管理;
    - spring-aspects: 与AspectJ集成,支持编译时织入,编译后织入,加载时织入;
- DA层-- Data Access/Integration(数据授权,融合)
    - spring-jdbc: jdbc抽象封装,简化jdbc操作;
    - spring-tx: 基于ext拓展层spring-aop的AOP的事务支持;
    - spring-orm: orm相关支持API,  Object/Relational mapping;
    - spring-oxm: Object/XML mapping,xml相关文件映射支持API;
    - spring-jms: Java Message Service, java消息服务API;
- web层
    - spring-web: web应用集成包,提供基本web远程服务支持;
    - spring-webmvc: 基于spring-web,对servlet支持的增强;
- Test:
    - spring-test: 支持spring组件与JUnit与TestNG的单元测试与集成测试,提供了spring的ApplicationContext缓存和可用于单独测试代码的模拟对象;

资料:
- https://www.jianshu.com/p/2225e872a164