
查看自定义源码实现：com.dowait.springSource.main.Main01

Spring的bean的生命周期：
    UserService类 -> 推断构造方法，Spring通过反射的方式创建bean对象 -> 注入依赖属性 -> 初始化前(@PostConstruct) -> 初始化(InitializingBean)
    -> 初始化后(AOP) -> 代理对象 -> bean
    * 通过反射的方式创建对象。推断使用哪个构造方法来创建对象，存在@Autowired修饰的构造方法时，通过这个构造方法创建对象，@Autowired最多
    只能作用在一个构造方法上，否则spring创建对象时会报错；如果没有@Autowired修饰的构造方法，则通过无参的构造方法创建对象；如果不存在无参
    的构造方法，通过有参的构造方法创建对象，这里要求类中只能包含一个有参的构造方法，否则Spring不知道通过哪个构造方法创建对象，会报错；
    * 注入依赖；
    * 初始化前。初始化前有两种方式实现，第一种是根据class对象筛选出标记了@PostConstruct注解的方法，然后通过反射的方式调用对象实例的这个
    方法，执行方式如下:method.invode(instance, null); 第二种方式是执行所有的BeanPostProcessor接口的实现类的
    postProcessBeforeInitialization方法；
    * 初始化，如果对象实现了InitializingBean接口，执行对象的afterPropertiesSet方法；
    * 初始化后，执行所有的BeanPostProcessor接口的实现类的postProcessAfterInitialization方法。通常AOP在这里实现，在这一步生成代理
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


Spring中自定义使用AOP：
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

AOP的实现方式汇总：
    1）原生的，可以使用JDK的动态代理，也可以使用CGLIB的动态代理，运行时动态生成代理对象。JDK的动态代理要求类必须实现接口，CGLIB的没有这个要求，
    CGLIB基于继承实现的代理对象，代理对象的类继承了原始类。
    2）Spring的AOP，运行时动态生成代理对象。通过注解方式实现AOP时，Spring借用了aspectjweaver的@Aspect、@Pointcut、@Before等注解。底层
    实际上依赖Spring的BeanPostProcessor实现的AOP功能。具体实现方式如下：
    对象初始化完成后，Spring从容器中找到所有的切面bean(标记了@Aspect注解的都是切面bean)，遍历每一个切面bean的class里的所有方法，
    解析@Befored等注解信息，判断表达式跟当前对象是否匹配，如果匹配，这个对象就要进行AOP。在BeanPostProcessor的postProcessAfterInitialization
    方法中，执行切面的@Before、真实对象的方法、切面的@After方法。



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


Spring事务及传播机制：
    1）在IOC容器中注册dataSource、transactionManager、jdbcTemplate等bean对象，可以通过 @Bean 注解来生成这些对象。
    2）在 @Configuration注解 的配置类中，通过 @EnableTransactionManagement 注解开启事务功能。
    3）在方法上通过 @Transactional 注解开启方法的事务。

    UserService userService = applicationContext.getBean(UserService.class);
    userService.test();
    上述两行的执行逻辑：
    1）从IOC容器中拿到的UserService是代理对象
    2）执行代理对象的test方法，代理对象的逻辑是判断方法上有@Transactional注解，开启事务，然后执行target.test方法即执行Spring创建的
    真正的UserService的test方法

将Spring的AOP和事务，融合理解一下。


mybatis：mybatis本身是一个独立的框架，免除了所有的jdbc代码，提升开发效率。
mybatis以SqlSessionFactory为核心，用户定义mapper接口，mybatis通过JDK的动态代理方式为接口生成代理对象来完成数据库操作。

myBatis-spring：将mybatis代码无缝的整合到Spring中。基础用法代码案例：
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
    // 实际上是调用了mybatis框架的代码，通过JDK的动态代理生成代理对象
    return sqlSessionTemplate.getMapper(UserMapper.class);
  }
}

总结起来，将UserMapper的代理对象注册到Spring容器，就可以在Service层面直接使用Mapper对象了，如下方的使用方式：
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;  // UserMapper在代码中定义的是接口，但程序运行时，这里的userMapper是mybatis通过jdk动态代理生成的代理对象
    public void test() {
        this.userMapper.test();
    }
}

创建bean的两种方式：
1）编程式。通过beanDefinition来定义bean
2）声明式。@Component、@Service、@Bean等注解，或者在xml中定义bean

疑问：spring-mybatis整合时，如何将mybatis的对象注册到spring容器。前半段，要将beanDefinition注册到applicationContext，spring-mybatis如何拿到applicationContext对象
mybatis是独立的框架，是org.mybatis下的。
Spring也是独立的框架，是org.springframework下的。
mybatis整合到Spring，是mybatis-spring组件来完成的，mybatis-spring是org.mybatis下的包。整合代码如下（github的ssm代码案例）：
    <bean id="scannerConfigurer" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!-- 扫描包 -->
        <property name="basePackage" value="pers.pole.mapper"/>
        <!-- 会话工厂名 -->
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
    1）在Spring的配置文件中注册bean，类型是MapperScannerConfigurer，是mybatis-spring中的类
    2）这个类实现了BeanDefinitionRegistryPostProcessor接口，生成bean时会回调postProcessBeanDefinitionRegistry方法
    3）在这个方法中，mapper的代理对象通过BeanDefinitionRegistry注册到容器
    4）Service层用的时候就可以从容器中拿到mapper的bean代理对象了

疑问：Springboot如何将mybatis的mapper对象放入容器？
mybatis-spring-boot-autoconfigure包的spring.factories文件内容是org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration，
表示要解析MybatisAutoConfiguration类，其中包含@Import({MybatisAutoConfiguration.AutoConfiguredMapperScannerRegistrar.class})，
也就会调用AutoConfiguredMapperScannerRegistrar类的registerBeanDefinitions方法，然后将mapper的代理对象通过BeanDefinitionRegistry
注册到容器。

疑问：Springboot的各种注解是什么意思？比如：@ConditionalOnMissingBean、@Import
@Import，以mybatis-spring-boot-autoconfigure包中的MybatisAutoConfiguration类的MapperScannerRegistrarNotFoundConfiguration上的@Import注解为例
