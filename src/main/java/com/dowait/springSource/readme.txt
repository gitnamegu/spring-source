
查看自定义源码实现：com.dowait.springSource.main.Main01

Spring的bean的生命周期：
    UserService类 -> 推断构造方法，Spring通过反射的方式创建bean对象 -> 注入依赖属性(依赖注入) -> 初始化前(@PostConstruct) -> 初始化(InitializingBean)
    -> 初始化后(AOP) -> 代理对象 -> bean

    * 通过反射的方式创建对象，推断使用哪个构造方法创建对象。
    存在@Autowired修饰的构造方法时，通过这个构造方法创建对象。这里要求@Autowired最多只能作用在一个构造方法上，否则spring创建对象时不知道用哪个，运行时会报错；
    不存在@Autowired修饰的构造方法，则通过无参的构造方法创建对象；
    不存在无参的构造方法，通过有参的构造方法创建对象，这里要求类中只能包含一个有参的构造方法，否则Spring创建对象时，不知道通过哪个，运行时会报错；
    * 注入依赖。
    * 初始化前。
    初始化前有两种方式实现，第一种是根据class对象筛选出标记了@PostConstruct注解的方法，然后通过反射的方式调用对象实例的这个
    方法，执行方式如下:method.invode(instance, null)，@PostConstruct只能用于无参的方法上; 第二种方式是执行所有的
    BeanPostProcessor接口的实现类的postProcessBeforeInitialization方法；
    * 初始化。如果类实现了InitializingBean接口，也就是对象实现了InitializingBean接口，执行对象的afterPropertiesSet方法；
    * 初始化后。执行所有的BeanPostProcessor接口的实现类的postProcessAfterInitialization方法。通常AOP在这里实现，在这一步生成代理
    对象，用来处理事务等场景；
    * 放入单例池，如果对象的scope是单例，将对象放入单例池，如果初始化后生成代理对象，放入单例池的就是代理对象，后续用的时候也是代理对象。

@Autowired注解：可以用在属性上，也可以用在构造方法上。
    用在属性上时，如果属性的类型只存在一个对象，根据类型去找bean完成依赖注入。如果类型存在多个bean对象，先byType根据类型去找bean，然后
再byName通过名字去找bean。
    用在构造方法上时，spring构造这个bean对象时，就通过这个构造方法来构造bean。如果这个构造方法有传参，传参的查找方式跟用在属性上时的查找bean
的方式一样。

    查找方式举例：
    1）这个类型存在多个bean对象，但是@Autowired的名字没有匹配上，比如@Autowired用于属性 private RoleService roleService12; ，而
    存在多个RoleService的bean对象但是没有名字为roleService12，就会抛异常。
    2）这个类型只存在一个bean，无论名字是否能匹配上，都能找到bean。

    构造方法的案例：
    @Autowired
    public UserService(OrderService orderService) {
        // 这里初始化UserService对象时，就会通过这个构造方法来初始化。orderService传参的值的查找方式是，先通过OrderService寻找bean
        集合，然后再通过orderService名字寻找bean。
    }

@Bean注解，没有通过value指定bean的名字时，产生的bean的名字默认是方法名。验证场景如下：
    @Configuration
    public class ApplicationConfig {
        @Bean
        public RoleService roleService() {
            return new RoleServiceImpl();
        }
    }

    public class Demo01 {
        public static void main(String[] args) {
            AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.dowait.spring");
            // 根据名字和类型获取，可以获取到对象
            RoleService roleService2 = applicationContext.getBean("roleService", RoleService.class);
            // 获取不到对象，因为名字不匹配
            RoleService roleService = applicationContext.getBean("roleServiceImpl", RoleService.class);
            System.out.println(roleService);
            System.out.println(roleService2);
            //roleService.test();
        }
    }

@Service注解，没有通过value指定bean的名字时，产生的bean的名字是类名的首字母小写。验证场景如下：
    @Service
    public class RoleServiceImpl implements RoleService {
        @Override
        public void test() {
            System.out.println("test...");
        }
    }

    public class Demo01 {
        public static void main(String[] args) {
            AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.dowait.spring.service");
            // 根据名字和类型获取，可以获取到对象
            RoleService roleService = applicationContext.getBean("roleServiceImpl", RoleService.class);
            // 获取不到对象，因为名字不匹配
            RoleService roleService = applicationContext.getBean("roleService", RoleService.class);
            roleService.test();
        }
    }


Scope为Single时，并不意味着这个类只有一个实例。比如以下场景，就会存在多个bean对象。
    @Component
    @Scope("singleton")
    public class UserService {

    }

    @Configuration
    public class Config {
        @Bean
        public UserService userService2() {

        }
    }
    这种情况下，UserService就存在两个bean。


在Spring项目中，自定义实现AOP动态代理的几种方式：
1）原生的AOP，JDK或CGLIB
2）Spring提供的AOP功能，通过@Aspect切面注解的方式自定义实现AOP
3）实现Spring的BeanPostProcessor接口，在postProcessAfterInitialization方法中自定义实现AOP

* 原生的AOP：可以使用JDK的动态代理，也可以使用CGLIB的动态代理，运行时动态生成代理对象。JDK的动态代理通过实现原始类的接口实现，CGLIB动态代理通过继承原始类实现。
    JDK的动态代理：获取到被代理对象的接口，动态生成代理类，代理类实现了这些接口，重写接口方法，然后基于代理类生成代理对象。
    1）获取到被代理对象的class引用，通过反射获取到所有实现的接口
    2）JDK提供了Proxy类，可以动态重新生成一个新的类，新的类实现了原始类的所有接口，增加了代理逻辑代码
    3）将新生成的类编译为.class文件，加载到JVM运行

    CGLIB动态代理：采用了非常底层的字节码技术，通过字节码技术为一个类生成一个子类，织入横切逻辑。因为CGLIB是通过继承目标类重写其方法实现的，所有final和private方法无法被重写，也就无法被代理。
    CGLIB的实现逻辑：
        class UserServiceProxy extends UserService {
            UserService target;
            public void test() {
                // 执行Before切面逻辑
                // 执行目标对象的test方法
                target.test();
            }
        }
        根据UserServiceProxy类创建代理对象，将原始对象赋值到代理对象的target属性。后续就可以执行代理对象的test方法了。

* 基于Spring提供的AOP功能：
    Spring借用了aspectjweaver的@Aspect、@Pointcut、@Before等注解。AOP真正的实现是通过Spring的BeanPostProcessor来做到的，具体实现过程如下：
    1）@EnableAspectJAutoProxy，开启AOP功能。这个注解是spring-context包的注解，spring-context包依赖了spring-aop。
    这个注解import了AspectJAutoProxyRegistrar配置类，这个配置类是ImportBeanDefinitionRegistrar的实现类，ImportBeanDefinitionRegistrar
    是Spring框架的一个扩展单元，允许开发者在运行时动态的往容器中注册bean，AspectJAutoProxyRegistrar配置类往容器中注入了
    AnnotationAwareAspectJAutoProxyCreator类的bean，这个bean是BeanPostProcessor的实现类的bean，也就是往容器中注入了
    一个BeanPostProcessor；（ImportBeanDefinitionRegistrar的应用demo参考MineImportBeanDefinitionRegistrar）
    2）bean初始化后，就会调用这个BeanPostProcessor的postProcessAfterInitialization方法，生成代理bean。具体方式是，Spring从容器中
    找到所有的切面bean即标记了@Aspect注解的都是切面bean，解析@Befored等注解信息，判断表达式跟当前对象是否匹配，如果匹配，这个对象就要进行AOP，
    也就是在BeanPostProcessor的postProcessAfterInitialization方法中，执行切面的@Before、真实对象的方法、切面的@After方法。
    Spring AOP的应用方式：
    1）添加依赖
    <dependency>
        <groupId>org.aspectj</groupId>
        <artifactId>aspectjweaver</artifactId>
        <version>1.9.5</version>
    </dependency>
    2）开启AOP功能
    @EnableAspectJAutoProxy
    3）编写切面逻辑，声明切点位置
    @Component
    @Aspect
    public class DemoAspect {
        // 这里的切点的表达支持正则表达式。
        @Pointcut("execution(public void com.dowait.spring.service.impl.RoleServiceImpl.test())")
        public void pointcut1() {
        }
        @Pointcut("execution(public void com.dowait.spring.service.impl.RoleServiceImpl.test2())")
        public void pointcut2() {
        }
        @Before("pointcut1() || pointcut2()")
        public void before(JoinPoint joinPoint) {
            System.out.println("aspect before 方法被执行1");
        }
    }
    另外，自定义切面中，切点的表达除了通过类和方法名表示外，还支持注解表达。比如自定义实现 annoDemo 注解，在切面中通过 @Pointcut("@annotation(com.dowait.aop.annoDemo)")
    来声明切点的位置，然后在代码的切点处加上这个注解就可以。
    4）代码的切点
    @Service
    public class RoleServiceImpl implements RoleService {
        @Override
        public void test() {
            System.out.println("RoleServiceImpl 的 test 方法 被执行");
        }
    }
    5）执行主程序，查看AOP的效果
    public class Demo01 {
        public static void main(String[] args) {
            AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.dowait.spring");
            RoleService roleService = applicationContext.getBean(RoleService.class);
            // 在这行打个断点，可以看到roleService是代理对象，而不是原始对象
            roleService.test();
        }
    }

* 自定义实现BeanPostProcessor接口，加上@Component注解，注入容器。然后在postProcessAfterInitialization方法中自定义实现AOP。具体应用方式如下：
    @Component("businessPostProcessor")
    public class BusinessPostProcessorMine implements BeanPostProcessor {
        // bean初始化前的处理
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            // 假设需求是对userService特殊处理
            if (beanName.equals("userService")) {
                UserServiceImpl userService = (UserServiceImpl) bean;
                System.out.println("对userService的处理");
            }
            return bean;
        }
        // bean初始化完成后的处理
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (beanName.equals("userService")) {
                // JDK的动态代理生成UserService的代理对象
                Object proxyInstance = Proxy.newProxyInstance(BusinessPostProcessorMine.class.getClassLoader(),
                        bean.getClass().getInterfaces(),
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                System.out.println("这是代理的逻辑，可以自定义实现，比如可以开启事务");
                                // 通过反射的方式执行bean的方法，也就是执行原始方法的业务逻辑
                                return method.invoke(bean, args);
                            }
                        });
                return proxyInstance;
            }
            return bean;
        }
    }

Spring事务及传播机制（AOP在事务中的应用）：
    使用方式：
    1）在IOC容器中注册dataSource、transactionManager、jdbcTemplate等bean对象，可以通过 @Bean 注解来生成这些对象；
    2）在 @Configuration注解 的配置类中，通过 @EnableTransactionManagement 注解开启事务功能；
    3）在方法上通过 @Transactional 注解开启方法的事务。

    UserService userService = applicationContext.getBean(UserService.class);
    userService.test();
    上述两行的执行逻辑（在AOP中添加事务逻辑）：
    1）从IOC容器中拿到的UserService是代理对象
    2）执行代理对象的test方法，代理对象的逻辑是判断方法上有@Transactional注解，开启事务，然后执行target.test方法即执行Spring创建的
    真正的UserService的test方法

    @EnableTransactionManagement注解的实现原理：
    1）这个注解是spring-tx依赖包中的注解，这个注解@Import({TransactionManagementConfigurationSelector.class})导入了配置类
    2）TransactionManagementConfigurationSelector -> AutoProxyRegistrar -> AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry)
    -> InfrastructureAdvisorAutoProxyCreator -> AbstractAdvisorAutoProxyCreator -> AbstractAutoProxyCreator
    -> SmartInstantiationAwareBeanPostProcessor -> InstantiationAwareBeanPostProcessor -> BeanPostProcessor，
    可以看到最终调用到了BeanPostProcessor，AbstractAutoProxyCreator类的postProcessAfterInitialization方法 -> wrapIfNecessary方法
    -> createProxy方法，在createProxy方法中创建了代理对象。
    AbstractAutoProxyCreator类的方法调用链中，Spring检查bean是否需要被代理，也就是查看这个bean是否包含@Transactional注解的方法，
    如果一个bean被标记为需要事务，AbstractAutoProxyCreator会对这个bean创建一个代理，实现事务逻辑。






Spring与第三方框架整合：
Spring与mybatis的整合：
    mybatis：mybatis本身是一个独立的框架，免除了所有的jdbc代码，提升开发效率。
    mybatis以SqlSessionFactory为核心，用户定义mapper接口，mybatis通过JDK的动态代理方式为接口生成代理对象来完成数据库操作。

    myBatis-spring：将mybatis代码无缝的整合到Spring中。
    基础用法代码案例：
    // 定义sqlSessionFactoryBean的bean对象
    @Configuration
    public class MyBatisConfig {
      @Bean
      public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        return factoryBean.getObject();
      }
    }
    // 定义Mapper的bean对象
    @Configuration
    public class MyBatisConfig {
      @Bean
      public UserMapper userMapper() throws Exception {
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory());
        // 实际上是调用了mybatis框架的代码，mybatis通过JDK的动态代理生成代理对象
        return sqlSessionTemplate.getMapper(UserMapper.class);
      }
    }
    // 应用
    @Service
    public class UserService {
        @Autowired
        private UserMapper userMapper;  // UserMapper在代码中定义的是接口，但程序运行时，这里的userMapper是mybatis通过jdk动态代理生成的代理对象
        public void test() {
            this.userMapper.test();
        }
    }
    总结起来，将UserMapper的代理对象注册到Spring容器，就可以在Service层面直接使用Mapper对象了，如下方的使用方式：

    这是简易版的使用方式，存在的问题是每写一个Mapper，开发者就去定义一个bean，这样太麻烦，所以spring-mybatis提供了MapperScannerConfigurer类，
    支持配置mapper路径，应用方式如下：
    <bean id="scannerConfigurer" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!-- 扫描包 -->
        <property name="basePackage" value="pers.pole.mapper"/>
        <!-- 会话工厂名 -->
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
    1）在Spring的配置文件中注册MapperScannerConfigurer类型的bean，是mybatis-spring中的类
    2）这个类实现了BeanDefinitionRegistryPostProcessor接口，所以生成MapperScannerConfigurer的bean时会回调postProcessBeanDefinitionRegistry方法
    3）在这个方法中，mapper的代理对象通过BeanDefinitionRegistry注册到容器
    4）Service层用的时候就可以从容器中拿到mapper的bean代理对象了

    这里涉及到一个知识点，Spring创建bean的两种方式：
    1）声明式。@Component、@Service、@Bean等注解，或者在xml中定义bean；
    2）编程式。通过beanDefinition来定义bean。其实声明式的底层，也是通过编程式实现的。
    spring-mybatis包在将mapper的代理对象时，如何注册到容器？就是通过编程式的方式来完成。

    总结：mybatis是独立的框架，是org.mybatis下的。Spring也是独立的框架，是org.springframework下的。mybatis整合到Spring，是
    mybatis-spring组件来完成的，mybatis-spring是org.mybatis下的包。



    疑问：Springboot如何将mybatis的mapper对象放入容器？
    mybatis-spring-boot-autoconfigure包的spring.factories文件内容是org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration，
    表示要解析MybatisAutoConfiguration类，其中包含@Import({MybatisAutoConfiguration.AutoConfiguredMapperScannerRegistrar.class})，
    也就会调用AutoConfiguredMapperScannerRegistrar类的registerBeanDefinitions方法，然后将mapper的代理对象通过BeanDefinitionRegistry
    注册到容器。

    疑问：Springboot的各种注解是什么意思？比如：@ConditionalOnMissingBean、



Spring-context的注解：
@import：是Spring的一个重要扩展点，作用是给容器中导入组件，可以在组件上使用@import注解。具体的使用场景：
1）@import导入一个实现了 ImportBeanDefinitionRegistrar 接口的类的bean，然后回调registerBeanDefinitions方法，开发者可动态往Spring容器中注入bean。
Spring-AOP的@EnableAspectJAutoProxy注解内部就用到了@import；
public class MineImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    public MineImportBeanDefinitionRegistrar() {
        System.out.println("====");
    }
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ImportBeanDefinitionRegistrar.super.registerBeanDefinitions(importingClassMetadata, registry);
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
        beanDefinition.setBeanClass(User.class);
        // 将beanDefinition注册到容器
        registry.registerBeanDefinition("user5", beanDefinition);
        //User bean = applicationContext.getBean(User.class);
    }
}

@Component
@Import(MineImportBeanDefinitionRegistrar.class) // 在组件上使用@Import注解
public class Component1 {
    @Bean
    public ComDemo comDemo() {
        return new ComDemo();
    }
}
2）@import导入一个实现了 ImportSelector 接口的类的bean
3）@import导入一个配置类
@Configuration(proxyBeanMethods = false)
public class UserConfig {
    @Bean
    public User user() {
        return new User("anna", 18);
    }
}

@ComponentScan("com.ys.entity")
@Import(UserConfig.class)
public class AppConfig {
}

public class Demo01 {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        User user = applicationContext.getBean("user", User.class);
        System.out.println(user);
    }
}

4）@import导入一个普通的java类，就会自动生成这个类的bean，用的不多

@import在Spring源码中的使用场景举例：
1）@import在Spring-AOP源码中的用途是这样的，开启AOP的注解@EnableAspectJAutoProxy中@Import({AspectJAutoProxyRegistrar.class})，使用
到了@import注解，在Spring容器中动态注入了BeanPostProcessor类的bean。
2）在mybatis-spring-boot-autoconfigure包中的MybatisAutoConfiguration类的MapperScannerRegistrarNotFoundConfiguration上，也同样使用了@Import注解


Spring如何解决循环依赖
什么是循环依赖？
// A依赖了B
class A {
    public B b;
}
// B依赖了A
class B {
    public A a;
}
A对象依赖B对象，B对象依赖A对象，就是循环依赖，循环依赖非常简单。但是为什么到Spring语境中就很复杂呢？因为在Spring中，一个对象并不是简单new出来，
而是会经过bean的一系列生命周期，就是因为bean的生命周期，所以循环依赖在Spring语境中才变得复杂。在Spring中，出现循环依赖的场景很多，有的场景
Spring帮我们自动解决，有的场景需要程序员来解决。要明白Spring的循环依赖，首先要明白Spring的bean的生命周期。Spring生命周期可以简单的分为：
1）实例化，原始对象
2）填充属性
3）AOP
4）放入单例池
循环依赖的问题出现在第二步，即填充属性。按照Spring的生命周期来讲，Spring构建A对象时，要填充B属性，然后到单例池中查找B对象，单例池中没有，
Spring就会创建B对象的bean，然后创建B对象时，发现又依赖了A对象，单例池中没有A对象，然后再去创建A对象，就没完没了了，这就是循环依赖在生命
周期中的问题。
出现循环依赖的时候，Spring的解决方式：
1）实例化A对象，放入二级缓存，Map<beanName,原始对象>
2）填充B属性，然后触发创建B对象，实例化B对象，因为B对象依赖了A对象，从单例池中没有找到A对象，然后从二级缓存中找，找到了A原始对象。然后B对象
就能走完完整的生命周期，最后放入单例池。所以A对象的生命周期的第二步也能执行完。

二级缓存如何解决循环依赖？会不会出现一些问题，所以才采用三级缓存。
Spring为什么使用三级缓存解决循环依赖？

三级缓存：
    1）一级缓存，singletonObjects，是ConcurrentMap<beanName:单例bean对象>，如果有AOP时，这里放的就是代理对象，这里的对象是完整的所有属性都赋值好的对象；
    2）二级缓存，earlySingletonObjects，是HashMap结构，Map<beanName:代理对象|原始对象>，存放已经实例化但尚未完全初始化的bean，也就是正在创建中的对象，此时对象的属性还没有被完全赋值，是不完整的对象，
    所以不能放入一级缓存中，只能放入二级缓存。当二级缓存中的对象属性都赋值完成后，才会被Spring放入一级缓存；
    3）三级缓存，singletonFactories，是HashMap结构，Map<beanName:lambda表达式>，也就是存放bean的工厂对象，如果有AOP，根据lambda表达式可以生成代理对象；如果类没有切点，不需要进行AOP，根据lambda表达式可以生成原始对象

三级缓存，依据案例梳理执行逻辑，背景案例：
public class AService {
    private BService bService;
    private int id;
}
public class BService {
    private AService aService;
    private int id;
}
1）实例化AService生成原始对象 -> 将lambda表达式即工厂对象放入三级缓存singletonFactories
2）填充bService属性 -> 从单例池也就是一级缓存中找bService对象 -> 没有找到 -> 执行实例化BService的逻辑
3）实例化BService生成原始对象 -> 将lambda表达式即工厂对象放入三级缓存singletonFactories
4）填充BService对象的aService属性 -> 从单例池中查找aService -> 没有找到 -> （在二级缓存中没有找到AService对象） -> 从三级缓存中拿到AService的工厂对象
-> 通过工厂对象，执行AService的lambda表达式，获取到早期引用对象或者代理对象，放入二级缓存 -> 将A的早期引用注入到B对象中 -> bService填充其他属性，初始化，然后放入单例池
5）继续执行AService的生命周期，根据bean的名字从二级缓存找，找到了AService实例 -> 从一级缓存找到B实例注入到属性 -> 初始化A对象 -> 放入一级缓存
备注：第5步执行完成后，bService实例的AService属性也是最新的内容，代码验证如下：
public class Demo01 {
    public static void main(String[] args) {
        DemoA demoA = new DemoA();
        demoA.setId(1);
        // 二级缓存
        Map<String, DemoA> map = new HashMap<>();
        map.put("key1", demoA);
        // 创建DemoB
        DemoB demoB = new DemoB();
        demoB.setDemoA(demoA);
        // 从二级缓存中获取到实例，执行其他属性的赋值逻辑
        DemoA demoA1 = map.get("key1");
        demoA1.setId(2);
        // 发现demoB实例的demoA属性是最新值
        System.out.println(demoB);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DemoA {
        private DemoB demoB;
        private Integer id;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DemoB {
        private DemoA demoA;
        private Integer id;
    }

}


BeanFactory和FactoryBean的区别：
BeanFactory是Spring框架的核心接口之一，是Spring IOC容器的基础，负责创建和管理bean。BeanFactory是一个底层接口，通常开发者使用的是它的扩展接口 ApplicationContext，它提供了更多高级功能
FactoryBean用来封装复杂对象的创建逻辑，通常与BeanDefinitionRegistryPostProcessor一起使用，参考示例代码DemoFactoryBean。

Spring的设计模式：
工厂模式：BeanFactory
代理模式：AOP
单例模式：bean的singleton
责任链模式：BeanPostProcessor，按照优先级顺序处理

Spring中的bean是线程安全的吗？
不是线程安全，本身没有对bean做线程安全的处理。因为在Spring的使用场景中，bean一般是无状态的，比如Controller、Service。
如果要对有状态的bean做线程安全，可以考虑加锁；或者使用prototype作用域

Spring事务如何实现？
基于数据库底层的事务机制和AOP实现。

Spring的事务传播机制如何实现？
由Spring实现，通过不同的数据库连接实现。方法调用时，如果复用上一个方法的事务，就复用相同的数据库连接。

常用的SpringBoot注解，及其实现？
1、@SpringBootApplication，这个注解标识了一个SpringBoot工程，它实际上是另外三个注解的组合，分别是：
1）@SpringBootConfiguration，这个注解实际上就是一个@Configuration，标识启动类也是配置类
2）@EnableAutoConfiguration，向容器中导入一个AutoConfigurationImportSelector类的bean，扫描并加载ClassPath下spring.factories中定义的
自动配置类，将这些配置类自动加载为bean。以druid-spring-boot-starter为例，自动加载DruidDataSourceAutoConfigure类的bean，DruidDataSourceAutoConfigure类中
又定义了dataSource的bean
3）@ComponentScan，扫描路径，默认没有配置路径，所以默认扫描启动类所在的当前目录
2、@Bean、@Controller等注解

熟悉的源码：
HashMap/线程池/消息队列等

Jdk1.7到1.8，HashMap发生了什么变化？
1）1.7底层是数组+链表，1.8底层是数组+链表+红黑树，加红黑树的目的是提高HashMap插入和查询的整体效率
2）1.7链表插入使用的是头插法，1.8中链表的插入是尾插法。因为1.8中插入key和value时，需要判断链表的元素个数，需要遍历链表来统计元素个数，所以正好使用尾插法

Jdk1.7到1.8，JVM发生了什么变化？
1.7中存在永久代，1.8中没有永久代，替换它的是元空间。元空间占用的内存不是虚拟机内存，而是本地内存。

Spring的大致启动流程
1）首先进行扫描，扫描得到所有的BeanDefinition对象，并存在一个Map中
2）筛选出非懒加载的单例的BeanDefinition去创建bean。多例的bean需要在启动过程中创建，多例的bean在每次获取bean时利用BeanDefinition去创建
3）利用BeanDefinition创建bean就是bean的生命周期，推断构造方法 -> 实例化 -> 填充属性 -> 初始化前 -> 初始化 -> 初始化后
4）Spring启动过程中还会处理@Import等注解

什么时候@Transactional失效
Spring事务基于代理来实现，所以@Transactional注解的方法只有通过代理对象访问时才会生效。
private修饰的方法也会失效，因为CGLib基于父子类实现代理，子类即代理类无法重写private方法，所以也不会生效。

对AOP的理解
AOP对某些对象进行增强，在执行对象方法前做额外的一些事情，在OOP的设计下，可能会引入大量重复代码，而通过AOP可以避免大量代码的重复。

对IOC的理解
容器、控制反转、依赖注入。
容器，实际上是map

Spring Bean 的生命周期
1）解析类得到BeanDefinition
2）推断构造方法，实例化对象
3）对@Autowired注解的属性进行填充
4）回调Aware方法，比如BeanNameAware、BeanFactoryAware
5）调用BeanPostProcessor的初始化前的方法
6）调用初始化方法
7）调用BeanPostProcessor的初始化后的方法，这里会进行AOP
8）如果是单例的bean，放入单例池
9）Spring容器关闭时，调用DisposableBean中 destory()方法


