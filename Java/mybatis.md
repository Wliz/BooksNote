Mybatis是一个可以自定sql，存储过程和高级映射的持久层框架；

Mybati缓存：一级缓存和二级缓存；以及缓存在session中，默认开启；二级缓存存放在命名空间里，默认不打开；使用二级缓存属性，类需要实现Serializable序列化接口（用来保存对象状态），可在映射文件中配置]\<cache/>


## mybatis如何分页，分页插件原理？

- mybatis使用RowBounds对象分页，也可以直接编写sql，也可以使用mybatis的分页插件；

- 分页插件原理：实现mybatis接口，实现自定义插件，在插件的拦截方法内拦截待执行的sql，并重写sql；

## mybatis插件运行原理，如何编写一个插件？

1. mybatis仅仅可以编写针对ParameterHandler，ResultSetHanlder，StatementHandler, Exector这四个接口的插件，mybatis通过动态代理，为需要拦截的接口生成代理对象以实现接口方法的拦截功能，每当执行这4中接口对象的方法时，就会进入拦截方法，具体就是InvocationHanlder的invoke方法；
2. 实现mybatis的Itnerceptor接口并复写intercept方法，然后给插件编写注解，执行拦截对象；

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

不可以重载；因为通过到寻找xml对应的sql的时候是通过全限定类名+方法名的保存和寻找策略。

接口工作原理：jdk动态代理原理，运行时为dao生成proxy，代理对象会拦截接口方法，执行对应的sql返回数据；


## mybatis有哪些Execute处理器，区别？

1. SimpleExecutor: 执行一次update或select，开启一个statement对象，用完立即关闭；
2. RecuseExecutor: 执行update或select，以sql作为key查找statement，存在使用，不存在就创建，使用后不关闭statement对象，放到map中；
3. BatchExecutor: 批量处理

## mybatis的mapper接口调用要求？

- id相同
- 参数类型相同
- 结果类型相同