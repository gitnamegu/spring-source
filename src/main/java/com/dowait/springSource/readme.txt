
查看自定义源码实现：com.dowait.springSource.main.Main01

Spring的bean的生命周期：
    UserService类 -> 推断构造方法，Spring通过反射的方式创建bean对象 -> 注入依赖属性 -> 初始化前(@PostConstruct) -> 初始化(InitializingBean)
    -> 初始化后(AOP) -> 代理对象 -> bean

    * 通过反射的方式创建对象，推断使用哪个构造方法创建对象。
    存在@Autowired修饰的构造方法时，通过这个构造方法创建对象。这里要求@Autowired最多只能作用在一个构造方法上，否则spring创建对象时不知道用哪个，运行时会报错；
    不存在@Autowired修饰的构造方法，则通过无参的构造方法创建对象；
    不存在无参的构造方法，通过有参的构造方法创建对象，这里要求类中只能包含一个有参的构造方法，否则Spring创建对象时，不知道通过哪个，运行时会报错；
    * 注入依赖。
    * 初始化前。
    初始化前有两种方式实现，第一种是根据class对象筛选出标记了@PostConstruct注解的方法，然后通过反射的方式调用对象实例的这个
    方法，执行方式如下:method.invode(instance, null); 第二种方式是执行所有的BeanPostProcessor接口的实现类的
    postProcessBeforeInitialization方法；
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
    @Compoent
    @Scope("single")
    public class UserService {

    }

    @Configuration
    public class Config {
        @Bean
        public UserService userService() {

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
    是Spring框架的一个扩展带你，允许开发者在运行时动态的往容器中注册bean，AspectJAutoProxyRegistrar配置类往容器中注入了
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
@import：是Spring的一个重要扩展点，作用是给容器中导入组件，可用于以下用途：
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
@Import(MineImportBeanDefinitionRegistrar.class)
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