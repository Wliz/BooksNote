# Bean生命周期

Spring Bean的生命周期有且仅有以下4个阶段:
- 实例化(Instantiation) -- 对应构造方法
- 属性填充(Populate)    -- 对应setter方法 
- 初始化(Initialization)
- 销毁(Destruction)

执行顺序: 实例化 -> 属性填充 -> 初始化 -> 销毁

注: 只有初始化和销毁两个阶段是用户可以自定义拓展的两个阶段; 其他阶段需要借助Spring提供的各种拓展能力实现;

相关逻辑参考源码doCreateBean方法(位置:AbstractBeanFactory定义方法createBean,AbstractAutowireCapableBeanfactory实现),实现细节顺序调用相关三个方法,对应Bean的生命周期的前三个阶段:
- createBeanInstance: 实例化阶段
- populateBean: 属性填充阶段
- initializeBean: 初始化阶段
- ConfigurableApplicationContext#close(): 销毁阶段
```Java
	/**
	 * Actually create the specified bean. Pre-creation processing has already happened
	 * at this point, e.g. checking {@code postProcessBeforeInstantiation} callbacks.
	 * <p>Differentiates between default bean instantiation, use of a
	 * factory method, and autowiring a constructor.
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition for the bean
	 * @param args explicit arguments to use for constructor or factory method invocation
	 * @return a new instance of the bean
	 * @throws BeanCreationException if the bean could not be created
	 * @see #instantiateBean
	 * @see #instantiateUsingFactoryMethod
	 * @see #autowireConstructor
	 */
	protected Object doCreateBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {

		// Instantiate the bean.
		BeanWrapper instanceWrapper = null;
		if (mbd.isSingleton()) {
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		if (instanceWrapper == null) {
            // 根据name等实例化bean
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		// ......
		// Initialize the bean instance.
		Object exposedObject = bean;
		try {
            // 属性填充实例化Bean
			populateBean(beanName, mbd, instanceWrapper);
            // 初始化bean
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		// ......
	
	}
```
# 拓展点

Spring Bean生命周期相关拓展点很多,通过源码+分类方式方便记忆;

## 影响多个Bean的接口

实现这些接口的Bean会切入到多个Bean的生命周期,功能非常强大,如自动注入和AOP的实现与它们都有关系;以下是Spring扩展中最重要的两个接口(均位于spring-beans包下):

- BeanPostProcessor: 作用于Bean生命周期初始化前后(对应postProcessBeforeInitialization, postProcessAfterInitialization方法)
```Java
public interface BeanPostProcessor {
    @Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
    @Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
```
- InstantationAwareBeanPostProcessor(继承自BeanPostProcessor): 见名知意,作用于Bean生命周期实例化前后(对应before, after方法)
```Java
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {
    @Nullable
	default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

    default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}
    // ......
}
```

解析:

postProcessBeforeInstantiation调用点:(作用于Bean实例化之前)
```Java
// AbstractAutowireCapableBeanFactory#createBean

protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {

		try {
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            // 让BeanPostProcessors有机会返回一个代理Bean而非一个目标Bean实例,这里是实现AOP功能的关键点[AOP使用动态代理生成代理对象实现,但需要注意是否指定了targetSoruce,如果没有指定,使用的是afterInitialization方法生成的代理对象,否则使用beforeInstantation生成代理对象]
            // 源码进入可跟踪到调用点
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			if (bean != null) {
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
					"BeanPostProcessor before instantiation of bean failed", ex);
		}

		try {
			Object beanInstance = doCreateBean(beanName, mbdToUse, args);
			if (logger.isTraceEnabled()) {
				logger.trace("Finished creating instance of bean '" + beanName + "'");
			}
			return beanInstance;
		}
		// ......
	}
```
postProcessAfterInstantiation调用点: (作用于Bean实例化之后)
```Java
// AbstractAutowireCapableBeanFactory#populateBean(属性填充方法内,属性填充之前执行)
@SuppressWarnings("deprecation")  // for postProcessPropertyValues
	protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
		// ....
		// Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
		// state of the bean before properties are set. This can be used, for example,
		// to support styles of field injection.
        // 在设置属性之前，让任何 InstantiationAwareBeanPostProcessors 有机会修改 bean 的状态。例如，这可用于支持字段注入样式。
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
                // 实例化之后执行调用点
				if (!bp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
					return;
				}
			}
		}
		// ......
	}
```

## 只调用一次的接口
### Aware(发现)接口
Aware接口可以让我们拿到Spring的资源,根据Aware前缀见名知意拿到不同资源如BeanNameAware可以拿到Bean的name;

Aware接口都是在初始化阶段调用:
```Java
// AbstractAutowireCapableBeanFactory#initializeBean
protected Object initializeBean(String beanName, Object bean, @Nullable RootBeanDefinition mbd) {
    // 调用执行相关Aware接口方法,执行BeanNameAware, BeanClassLoaderAware,BeanFactoryAware三个Aware
		if (System.getSecurityManager() != null) {
			AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
				invokeAwareMethods(beanName, bean);
				return null;
			}, getAccessControlContext());
		}
		else {
			invokeAwareMethods(beanName, bean);
		}

		Object wrappedBean = bean;
		if (mbd == null || !mbd.isSynthetic()) {
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
		}

		try {
			invokeInitMethods(beanName, wrappedBean, mbd);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
				(mbd != null ? mbd.getResourceDescription() : null),
				beanName, "Invocation of init method failed", ex);
		}
		if (mbd == null || !mbd.isSynthetic()) {
			wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
		}

		return wrappedBean;
	}
```

#### 第一类
- BeanNameAware
- BeanClassLoaderAware
- BeanFactoryAware

注: 第一类三个Aware均是在初始化之前直接调用;

### 第二类
- EnvironmentAware
- EmbeddedValueResolverAware: 获取Spring的EL表达式解析器,用于自定义注解使用EL表达式时可以使用
- ApplicationContextAware(ResourceLoaderAware\ApplicationEventPublisherAware\MessageSourceAware): 这几个接口本质上都是当前ApplicationContext对象,ApplicationContext都实现相关接口;

注: 
- ApplicationContext相关Aware则是通过BeanPostProcessor#postProcessorBeforeInitilization实现;如ApplicationContextAwareProcessor
```Java
// ApplicationContextAwareProcessor
// 判断Bean是否实现了Aware相关方法(通过接口判断),如果实现了,则调用回调方法将资源传递给bean
@Override
	@Nullable
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		if (!(bean instanceof EnvironmentAware || bean instanceof EmbeddedValueResolverAware ||
				bean instanceof ResourceLoaderAware || bean instanceof ApplicationEventPublisherAware ||
				bean instanceof MessageSourceAware || bean instanceof ApplicationContextAware ||
				bean instanceof ApplicationStartupAware)) {
			return bean;
		}

		AccessControlContext acc = null;

		if (System.getSecurityManager() != null) {
			acc = this.applicationContext.getBeanFactory().getAccessControlContext();
		}

		if (acc != null) {
			AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                // 实现Aware接口方法,调用方法将资源传递给bean(主要是ApplicationContext赋值)
				invokeAwareInterfaces(bean);
				return null;
			}, acc);
		}
		else {
			invokeAwareInterfaces(bean);
		}

		return bean;
	}
```
- ApplicationContext与BeanFactory有什么区别? 答案可以根据源码ApplicationContext继承两个BeanFactory(bean与父类bean)以外接口功能回答即可;


Aware接口第一类在第二类之前执行,均是初始化前执行;

### 生命周期接口
Bean的实例化和属性填充是由Spring处理,用户可以自己处理有初始化和销毁两个阶段,分别对应两个接口:
- InitializingBean: 对应初始化阶段,因为Aware接口均是在初始化之前,可以放心使用Aware接口获取的资源,处理实现InitializingBean接口,还可以通过注解或xml文件方式指定初始化方法,一般只使用一种,不必纠结其执行顺序;
- DisposableBean: 对应销毁阶段,以ConfigurableApplicationContext#close作为入口,调用bean的destroy方法;

# BeanPostProcessor注册时机以及执行顺序

- 注册时机:
BeanPostProcessor本身也是Bean,那么就需要在业务bean初始化之前初始化完成,在源码中先进行注册,然后执行finishBeanFactoryInitialization初始化单例非懒加载bean;
- 执行顺序: BeanPostProcessor有很多,而且每个都会影响多个bean的初始化,所以执行顺序很重要,执行顺序引入了PriorityOrder(优先级最高)和Order(优先级其次)两个排序接口,最后才是其他普通;
    ```Java
    // AbstractApplicationContext#refresh -> PostProcessorRegistrationDelegate#registerBeanPostProcessor
    public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		// ......

		// First, register the BeanPostProcessors that implement PriorityOrdered.
        // 第一步,注册实现了PriorityOrdered接口的BeanPostProcessors
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
        // 第二步,注册实现了Ordered接口的BeanPostProcessors
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
        // 第三步,注册常规的BeanPostProcessors
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
        // 最后,再次注册内部BeanPostProcessors
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
        // 重新处理内部类作为ApplicationListeners,并移动到processor的处理链尾部
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}
    ```