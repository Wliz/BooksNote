# Spring Cloud

Spring Cloud流应用程序启动器是基于Spring Boot的Spring继承应用程序，是一个微服务框架;

是一系列框架的有序集合，利用Spring Boot的开发便利简化了分布式系统基础设施的开发，如服务注册发现，配置中心，服务网关，负载均衡，断路器，数据监控等，都可以用Spring Boot的开发风格做到一键启动和部署；

## Spring Cloud Netflix

Netflix OSS开源组件集成，包括Eureka，Ribbon，Hystrix，Feign，Zuul等组件；

- Eureka：服务治理组件，包括服务端注册中心和客户端的服务发现机制；
- Ribbon：负载均衡的的服务调用组件，具有多种负载均衡调用策略；（提供服务定位和客户端负载均衡）
- Hystrix：服务容错组件，实现断路器模式，为依赖服务的出错和延迟提供容错能力；（实现服务熔断，服务降级和资源隔离）
- Feign：基于ribbon和hystrix的声明式服务调用组件；
- zuul：API网关组件，对请求提供路由和过滤功能；

## 服务注册发现？
Eureka服务注册和发现，所有的服务都在Eureka服务器上注册并通过调用Eureka服务器完成查找；

## 负载均衡？
负载均衡可以改善跨计算机，计算机集群，网络链接等计算资源的工作负载分布。

旨在优化资源使用，最大化吞吐量，最小化响应时间避免单一资源过载。


# 重试相关组件

Spring Cloud与重试相关的组件如下：
- ribbon：实现服务定位和客户端负载均衡
- hystrix：实现服务熔断，服务降级和资源隔离
- feign：声明式Http客户端，用于服务之间的Http调用

hystrix（熔断器，断路器）是Spring Clound Circuit Breaker抽象的一种实现，阿里也提供了一种实现Sentinel；

# 三者关系

在超时重试方面，Feign和ribbon冲突，开发团队便将Feign的重试设置为默认的NEVER_RETRY（不重试），使用ribbon的重试处理，所以如果需要使用Feign重试的情况使用ribbon重试即可；

hystrix和ribbon是完全隔离的两层配置，hystrix主管熔断操作，ribbon主管服务定位和负载均衡（可配置）；两者均有超时时间配置，但相关概念不同；

当服务调用发生超时时，首先触发ribbon重试，重试触发了断路器超时时间配置，则直接停止服务调用（虽然此时服务调用正在执行）;

## Feign相关超时配置

两种配置：代码+属性

默认属性配置优先，而且全局属性配置名默认是default，可以设置feign.client.default-config为其他名字；

属性：
~~~yml
feign:
  hystrix:
    # hystrix开启（可能产生熔断效果）
    enabled: true
  client:
    config:
      # 全局配置
      default:
        connectTimeout: 5000
        readTimeout: 5000  
      # 实例配置，feignName即@feignclient中的value，也就是服务名
      feignName:
        connectTimeout: 5000
        readTimeout: 5000
~~~

## hystrix相关超时配置

默认属性配置优先

~~~yml
hystrix:
  command:
    #全局默认配置
    default:
      #线程隔离相关
      execution:
        timeout:
          #是否给方法执行设置超时时间，默认为true。一般我们不要改。
          enabled: true
        isolation:
          #配置请求隔离的方式，这里是默认的线程池方式。还有一种信号量的方式semaphore。
          strategy: THREAD
          thread:
            #方式执行的超时时间，默认为1000毫秒，在实际场景中需要根据情况设置
            timeoutInMilliseconds: 10000
    # 实例配置
    HystrixCommandKey:
      execution:
        timeout:
          enabled: true
        isolation:
          strategy: THREAD
          thread:
            timeoutInMilliseconds: 10000
~~~

## Ribbon相关超时配置

~~~yml
# 全局配置
ribbon:
 # （单个实例）服务最大重试次数,不包含第一次请求，默认0
 MaxAutoRetries: 5
 # 负载均衡切换次数,如果服务注册列表小于 nextServer count 那么会循环请求  A > B >　A，默认1
 MaxAutoRetriesNextServer: 3
 #是否所有操作都进行重试
 OkToRetryOnAllOperations: false
 #连接超时时间，单位为毫秒，默认2秒
 ConnectTimeout: 3000
 #读取的超时时间，单位为毫秒，默认5秒
 ReadTimeout: 3000
# 实例配置
clientName:
  ribbon:
   MaxAutoRetries: 5
   MaxAutoRetriesNextServer: 3
   OkToRetryOnAllOperations: false
   ConnectTimeout: 3000
   ReadTimeout: 3000
~~~

## 重试注意事项

- Feign超时和Ribbon超时配置：如果设置Feign超时，则会使用Feign超时配置，优先级高于Ribbon；Feign配置超时时间，Ribbon配置超时配置，则超时时间使用Feign，其他配置使用Ribbon；【建议使用Ribbon重试】

- 由于Hystrix存在，则设置Ribbon重试时间要小于Hystrix超时时间，否则会产生服务调用重试期间，直接被Hystrix停止调用（时间设置范围参考后续）；

- 重试次数：（MaxAutoRetries + 1） * （MaxAutoRetriesNextServer + 1）

- 重试时间：（MaxAutoRetries + 1） * （MaxAutoRetriesNextServer + 1）* （ReadTimeout + ConnectTimeout）

- hystrix设置timeoutInMilliseconds超时时间时不得小于重试时间；

