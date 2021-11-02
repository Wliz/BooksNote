Mybatis是一个可以自定sql，存储过程和高级映射的持久层框架；

mybatis理解：https://juejin.im/post/6844904087549378574

Mybati缓存：一级缓存和二级缓存；一级缓存在session中，默认开启；总二级缓存存放在命名空间里，默认打开，若使之生效需要在映射文件内部配置；使用二级缓存属性，类需要实现Serializable序列化接口（用来保存对象状态），可在映射文件中配置]\<cache/>【需要在对应的mapper文件中】


## mybatis如何分页，分页插件原理？

- mybatis使用RowBounds对象分页，也可以直接编写sql，也可以使用mybatis的分页插件；

- 分页插件原理：实现mybatis接口，实现自定义插件，在插件的拦截方法内拦截待执行的sql，并重写sql；

## mybatis插件运行原理，如何编写一个插件？

1. mybatis仅仅可以编写针对ParameterHandler，ResultSetHanlder，StatementHandler, Exector这四个接口的插件，mybatis通过动态代理，为需要拦截的接口生成代理对象以实现接口方法的拦截功能，每当执行这4中接口对象的方法时，就会进入拦截方法，具体就是InvocationHanlder的invoke方法；
2. 实现mybatis的Itnerceptor接口并复写intercept方法，然后给插件编写注解，执行拦截对象；

注：Executor主要负责维护一级缓存和二级缓存，并提供事务管理的相关操作，将数据库相关操作委托给StatementHandler完成。StatementHandler首先通过ParameterHandler完成sql语句参数绑定，通过statement对象执行sql语句并得到结果集，通过ResultSetHandler完成结果集映射，得到结果对象并返回；

## mybatis动态sql做什么？都有那些动态sql，简述一下动态rsql的执行原理？

1. 动态sql可以让我们在xml映射文件内，以标签形式编写动态sql，完成逻辑判断和动态拼接sql的功能；
2. 提供9中动态sql标签：trim|where|set|if|foreach|choose|when|otherwise|bind
3. 执行原理：使用ognl从sql参数对象中计算表达式的值，根据表达式的值动态拼接sql，完成动态sql功能；

## #{}和${}区别
1. #{}是预编译处理，${}是字符串替换；
2. mybatis替换#{}为？，调用PreparedStatement的set方法赋值；
3. ${}会被mybatis直接替换成变量值；
4. #{}预编译，可以防止sql注入，提供系统安全性；

## 为什么说mybatis是半自动orm映射框架？与全自动的区别在哪里？

hibernate属于全自动orm映射框架，使用hibernate查询关联对象或关联集合对象时，根据对象关系模型直接获取，所以为全自动；

而Mybatis查询关联对象或关联集合对象，需要手动编写sql，所以称之为半自动orm映射框架；

## mybatis是否支持延迟加载，若支持，实现原理？
1. mybatis支持association（一对一）和collection（一对多）延迟加载，需要在配置文件内配置是否启用延迟加载（LazyLoadingEnabled=true|false[默认false]）
2.原理：使用cglib创建目标对象的代理对象，当调用目标方法时，进入拦截器方法如a.getB().getName(),拦截器invoke发现getB是null，就会单独发送事先保存好的查询关联对象B的sql，把b查询出来，调用set方法设置进去，继续完成后续处理；

## mybatis接口绑定，好处？

接口绑定就是在mybatis中任意定义接口，将接口中方法和sql语句绑定，我可以就可以直接调用接口方法执行相应的sql；

好处：可以很灵活的选择和设置；

## 接口绑定几种实现方式？
1. 使用注解@Select之类的：sql语句比较简单适合
2. 使用xml文件配置（需要全限定类名）：sql语句复杂时

## 如何将sql执行结果映射到结果对象bean中
1. resultMap标签，定义映射关系
2. 使用sql别名，将列名定义对bean的字段名即可；

## 一个xml映射文件，都会写一个dao接口对应，dao工作原理，是否可以重载？

不可以重载；因为通过到寻找xml对应的sql的时候是通过接口名+方法名的保存和寻找策略。

常用动态代理包括JDK内置动态代理和基于CGlib等第三方组件的方式，mybatis采用JDK内置动态代理创建动态代理对象；

接口工作原理：jdk动态代理原理，运行时为dao生成proxy，代理对象会拦截接口方法，执行对应的sql返回数据；


## mybatis有哪些Executor处理器，区别？

1. SimpleExecutor: 执行一次update或select，开启一个statement对象，用完立即关闭；
2. RecuseExecutor: 执行update或select，以sql作为key查找statement，存在使用，不存在就创建，使用后不关闭statement对象，放到map中；
3. BatchExecutor: 批量处理

## mybatis的mapper接口调用要求？

- id相同
- 参数类型相同
- 结果类型相同

## mybatis核心流程
1. 初始化阶段：读取xml配置文件和注解中的配置信息，创建配置对象，并完成各个模块的初始化工作；
    - mybatis将所有配置文件经过解析后加载到重量级对象configuration对象中；

2. 代理阶段：封装模型，使用mapper接口开发的初始化工作；
    - 通过configuration对象构建sqlSession，然后通过动态代理获取对应的接口代理对象mapper，然后调用Executor执行器执行
3. 数据读取阶段：完成sql解析，参数映射，sql执行，结果反射解析过程；
    - 通过configuration中定义的结果映射处理器进行结果映射


## MyBatis源码解析

### Mybatis结构划分

- 接口层：定义了暴露给应用程序调用的API
    - sqlsession
- 核心处理层：实现了核心处理流程，包括mybatis初始化和完成一次数据操作涉及的全部流程
    - 配置解析
    - 插件
    - 参数映射
    - sql解析
    - sql执行
    - 结果集映射
- 基础支持层：为核心处理层提供良好的基础支撑，如反射，日志，事务等
    - 缓存
    - 数据源
    - 事务
    - 日志
    - 加载
    - ……
        
主要构件：
- SqlSession：mybatis顶层api，表示和数据库交互会话，完成相应功能
- Executor：执行器，mybatis调度核心，负责参数解析，sql执行，结果集映射等
- StatementHandler: 封装JDBC的Statement操作，负责结果集转换；
- ParameterHandler: 参数解析映射，将参数映射和实参绑定
- TypeHanlder: java和jdbc类型转换
- MappedStatement: 维护SQL语句的封装
- SqlSource: 动态生成sql语句，封装到BoundSql中
- BoundSql: 动态生成的sql和参数
- Configuration： 所有配置全部维持在该类实例对象中
