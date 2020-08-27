## Spring Boot优点？
- 减少开发，测试时间和努力
- 使用JavaConfig有助于避免使用xml
- 避免大量的maven导入和各种版本冲突（版本依赖管理）
- 内置web服务器 ，不需要启动Tomcat等
- 使用注解方式，减少xml文件配置

## JavaConfig?

JavaConfig是Spring社区产品，提供配置Spring IOC容器的纯Java方法，有助于避免使用xml配置；

优点：

面向对象配置：配置可以定义为JavaConfig中类，而且一个配置类可以继承另一个，并重写@Bean方法；

减少或消除xml文件配置：基于依赖注入原则优化配置；

## 如何重新加载Spring Boot上更改，而无需重新启动服务器？
可以使用DEV工具实现，开发工具DevTools模块

## Spring Boot中的监视器是什么？

Spring Boot actuator是Spring启动框架中的重要功能之一，可以帮助访问生产环境中正在运行的应用程序的当前状态。

## Spring Boot实现异常处理？

可以使用ControllerAdvice注解指定，处理控制器类抛出的异常;