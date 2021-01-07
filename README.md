
## Dubbo介绍

> Dubbo是阿里巴巴开源的一个高性能、轻量级的RPC框架

Dubbo的三个核心能力：

- 面向接口的远程方法调用；
- 只能容错和负载均衡；
- 服务自动注册和发现

官网地址：http://dubbo.apache.org/zh-cn/index.html



Dubbo的核心组件：

- Provider：服务提供方；
- Consumer：调用远程服务的服务消费方；
- Registry：服务注册与发现中心；
- Monitor：统计服务调用次数和调用时间的监控中心；
- Container：服务运行容器，负责启动、加载、运行服务提供者



调用关系说明：

- 虚线代表异步调用，实线代表同步访问；
- 蓝色虚线：在启动时完成功能；
- 红色虚线：程序运行中执行的功能



调用流程：

- 服务提供者在服务容器启动时，向注册中心注册自己提供的服务
- 服务消费者在启动时，向注册中心订阅自己所需的 服务；
- 注册中心向服务消费者返回服务提供者地址列表，如果有列表变更，会基于长连接推送变更数据给消费者
- 服务消费者从提供者地址列表中基于软负载均衡算法选择一台提供者进行调用，如果调用失败，则重新选择一台

在调用过程中，服务提供者和消费者在内存中的调用次数和调用时间等信息会定时每分钟发送到监控中心进行记录。



Dubbo官方推荐使用Zookeeper来作为服务注册与发现中心。



## Dubbo实战

Dubbo中的所有服务调用都是基于双方约定好的接口进行调用的。

Dubbo开发有三种形式：

- 基于XML配置
- 基于注解配置
- java硬编码



java硬编码方式适合需要基于Dubbo实现自定义框架的场景，XML配置方式在生产中比较常见，因此下文中会以XML配置方式来实现Dubbo案例实战。

dubbo开发基本步骤有：

- 定义API模块，用于规范双方接口协定；
- 提供Provider模块，引入API模块依赖，并实现API模块中需要暴露的接口，并将其注册到注册中心上，对外来统一提供服务；
- 提供Consumer模块，引入API模块，并且引入与提供者相同的注册中心，并进行服务调用。



以下是详细的实现步骤：

- 创建项目`dubbodemo`并导入maven坐标：

```
  <properties>
      <dubbo.version>2.7.5</dubbo.version>
    </properties>
  
    <dependencyManagement>
      <dependencies>
        <dependency>
          <groupId>com.liu</groupId>
          <artifactId>service-api</artifactId>
          <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
          <groupId>org.apache.dubbo</groupId>
          <artifactId>dubbo</artifactId>
          <version>${dubbo.version}</version>
          <exclusions>
            <exclusion>
              <groupId>org.apache.dubbo</groupId>
              <artifactId>dubbo-common</artifactId>
            </exclusion>
          </exclusions>
        </dependency>
        <dependency>
          <groupId>org.apache.dubbo</groupId>
          <artifactId>dubbo-common</artifactId>
          <version>${dubbo.version}</version>
        </dependency>
        <dependency>
          <groupId>org.apache.dubbo</groupId>
          <artifactId>dubbo-registry-zookeeper</artifactId>
          <version>${dubbo.version}</version>
          <exclusions>
            <exclusion>
              <groupId>org.apache.dubbo</groupId>
              <artifactId>dubbo-remoting-api</artifactId>
            </exclusion>
            <exclusion>
              <groupId>org.apache.dubbo</groupId>
              <artifactId>dubbo-common</artifactId>
            </exclusion>
          </exclusions>
        </dependency>
        <dependency>
          <groupId>org.apache.dubbo</groupId>
          <artifactId>dubbo-registry-nacos</artifactId>
          <version>${dubbo.version}</version>
        </dependency>
        <dependency>
          <groupId>org.apache.dubbo</groupId>
          <artifactId>dubbo-rpc-dubbo</artifactId>
          <version>${dubbo.version}</version>
          <exclusions>
            <exclusion>
              <groupId>org.apache.dubbo</groupId>
              <artifactId>dubbo-remoting-api</artifactId>
            </exclusion>
            <exclusion>
              <groupId>org.apache.dubbo</groupId>
              <artifactId>dubbo-common</artifactId>
            </exclusion>
          </exclusions>
        </dependency>
        <dependency>
          <groupId>org.apache.dubbo</groupId>
          <artifactId>dubbo-remoting-netty4</artifactId>
          <version>${dubbo.version}</version>
          <exclusions>
            <exclusion>
              <groupId>org.apache.dubbo</groupId>
              <artifactId>dubbo-remoting-api</artifactId>
            </exclusion>
          </exclusions>
        </dependency>
        <dependency>
          <groupId>org.apache.dubbo</groupId>
          <artifactId>dubbo-serialization-hessian2</artifactId>
          <version>${dubbo.version}</version>
          <exclusions>
            <exclusion>
              <groupId>org.apache.dubbo</groupId>
              <artifactId>dubbo-common</artifactId>
            </exclusion>
          </exclusions>
        </dependency>
      </dependencies>
    </dependencyManagement>
```

  

- 创建API项目`service-api`并定义接口`HelloService`：

```
  public interface HelloService {
    String sayHello(String name);
  }
```

- 创建Provider项目`service-provider`，引入API以及dubbo依赖，并实现提供远程调用的接口：

```
  	<dependency>
        <groupId>com.lagou</groupId>
        <artifactId>service-api</artifactId>
        <version>1.0-SNAPSHOT</version>
      </dependency>
     //...dubbo相关依赖
```

  

```
  public class HelloServiceImpl implements HelloService {
  
    @Override
    public String sayHello(String name) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return "hello : " + name;
    }
  }
```

  

- 在XML文件`dubbo-provider.xml`中配置可供远程调用的接口信息：

```
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
    xsi:schemaLocation="http://www.springframework.org/schema/beans        http://www.springframework.org/schema/beans/spring-beans-4.3.xsd        http://dubbo.apache.org/schema/dubbo        http://dubbo.apache.org/schema/dubbo/dubbo.xsd">
    <!-- 提供方应用信息，用于计算依赖关系 -->
    <dubbo:application name="service-provider" owner="liujiao" />
  
    <!-- 使用multicast广播注册中心暴露服务地址 -->
    <dubbo:registry address="zookeeper://106.75.177.44:2181" />
  
    <!-- 用dubbo协议在20880端口暴露服务 -->
    <dubbo:protocol name="dubbo" port="20885" />
  
    <!-- 声明需要暴露的服务接口 -->
    <dubbo:service interface="com.liu.service.HelloService" ref="helloService" />
  
    <!-- 和本地bean一样实现服务 -->
    <bean id="helloService" class="com.liu.service.impl.HelloServiceImpl" />
  </beans>
```

  

- 定义Consumer项目`service-consumer`，引入API依赖，并编写远程调用逻辑：

```
  ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:dubbo-consumer.xml");
      context.start();
      HelloService bean = context.getBean(HelloService.class);
      String liujiao = bean.sayHello("liujiao");
      System.out.println(liujiao);
      System.in.read();
```

  

- 在XML文件`dubbo-consumer.xml`中配置消费调用方细节：

```
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
         xsi:schemaLocation="http://www.springframework.org/schema/beans        http://www.springframework.org/schema/beans/spring-beans-4.3.xsd        http://dubbo.apache.org/schema/dubbo        http://dubbo.apache.org/schema/dubbo/dubbo.xsd">
  <!-- 提供方应用信息，用于计算依赖关系 -->
  <dubbo:application name="service-consumer"   owner="hhhh">
      <!--运维使用的端口，如果provider启动后会默认占用22222这个端口，因此该项目启动会提示该端口被占用，不影响启动，我们可以在这指定其他端口-->
      <dubbo:parameter key="qos.port" value="23232"></dubbo:parameter>
  </dubbo:application>
  
  <!-- 使用multicast广播注册中心暴露服务地址 -->
  <dubbo:registry address="zookeeper://106.75.177.44:2181" />
  <!--timeout：调用超时
  mock:mock="true" 当调用接口超时时，会调用mock类返回mock定义的返回值
  reties：请求失败后的重试次数：（注意：需要保证接口幂等）-->
      <dubbo:consumer  timeout="1000" retries="3"/>
  <dubbo:protocol name="dubbo"/>
  <!--mock也可以在某个service引用上定义：类包名规则：com.liu.service.HelloServiceMock"-->
  <dubbo:reference id ="helloService" interface="com.liu.service.HelloService"/>
  </beans> 
```

  需要注意的是，在服务提供方和消费方的XML中配置的注册中心协议和地址必须保持一致，其他相关的细节配置说明在XML文件中已经作了详细注释。



- 启动：先启动服务提供方main方法， 然后启动服务消费方main方法，在控制台打印：

```
  hello : liujiao
```

  证明已经成功完成dubbo远程调用。

  备注：在启动之前，必须保证zookeeper已经启动，并且能够访问。

  

代码地址：https://github.com/liujiao111/dubbodemo

## Dubbo高级



### SPI机制



> SPI 全称为 (Service Provider Interface) ，是JDK内置的一种服务提供发现机制。 目前有不少框架用它 来做服务的扩展发现，简单来说，它就是一种动态替换发现的机制。使用SPI机制的优势是实现解耦， 使得第三方服务模块的装配控制逻辑与调用者的业务代码分离。

##### JAVA SPI



实现步骤：

- 定义接口

  新建一个module名为`java-spi-api`，定义接口`DemoService`：

```
  public interface DemoService {
      String sayHello();
  }
```

  

- 定义实现类，并根据SPI约定进行配置

  新建module名为`java-spi-impl`，并导入模块`java-spi-api`依赖，编写实现类`DemoServiceImpl`：

```
  public class DemoServiceImpl implements DemoService {
      @Override
      public String sayHello() {
          return "Hello Java Spi!!";
      }
  }
```

  在resources目录下新建一个目录`/META-INF/services`，在该目录下创建一个以接口全限定名为名称的文件，在该文件中写入实现类的全限定名。

- 通过SPI获取实现类并调用

  新建module名为`java-spi-main`，导入`java-spi-impl`和`java-spi-api`的依赖，并利用SPI获取类，调用

```
  public class SpiMain {
  
      public static void main(String[] args) {
          ServiceLoader<EmailService> demoServices = ServiceLoader.load(EmailService.class);
          for (EmailService demoService : demoServices) {
              System.out.println(demoService.sayHello("dfdd"));
          }
      }
  }
```



##### Dubbo Spi

Dubbo Spi可以通过Adaptice动态加载URL指定的接口。



实现步骤：

- 与Java Spi实现类似，接口上需要添加`@SPI`注解，方法上添加`@Adaptive`注解，并且在接口中传入形参`org.apache.dubbo.common.URL`,如下所示：

```
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;

@SPI
public interface EmailService {
    @Adaptive
    String sayHello(URL url);
}
```

- 在SPI配置文件中指定实现类的名称

```
  email1=com.liu.service.impl.EmailServiceImpl
```

  

- 使用Dubbo Spi来获取实现类并完成调用

```
  import com.liu.EmailService;
  import org.apache.dubbo.common.URL;
  import org.apache.dubbo.common.extension.ExtensionLoader;
  
  public class DubboSpiMain {
  
      public static void main(String[] args) {
          URL url = URL.valueOf("test://localhost/email?email.service=email2");
          EmailService activateExtension = ExtensionLoader.getExtensionLoader(EmailService.class).getAdaptiveExtension();
          System.out.println(activateExtension.sayHello(url));
      }
  }
```

  注意： 

  - 因为在这里只是临时测试，所以为了保证URL规范，前面的信息均为测试值即可，关键的点在于 email.service 参数，这个参数的值指定的就是具体的实现方式。关于为什么叫 email.service 是因为这个接口的名称，其中后面的大写部分被dubbo自动转码为 . 分割。
  - 通过 getAdaptiveExtension 来提供一个统一的类来对所有的扩展点提供支持(底层对所有的扩展 点进行封装)。 
  - 调用时通过参数中增加 URL 对象来实现动态的扩展点使用。
  -  如果URL没有提供该参数，则该方法会使用默认在 SPI 注解中声明的实现。



Dubbo Spi底层实现是动态代理。

Dubbo Spi与Java Spi的区别：

- Java Spi会一次性加载所有的扩展点，比较耗时，浪费资源,Dubbo Spi可以根据所需进行动态加载，无需全部加载；
- Java Spi加载时如果有扩展点加载失败，则所有扩展点都不可用，Dubbo中每个扩展点相互独立，加载互不影响；
- Dubbo Spi提供了对扩展点包装的功能，并且支持通过set方式对其他扩展点进行注入。



### Dubbo负载均衡

负载均衡是指将请求分摊到多个操作单元上进行执行，从而共同完成工作任务。



Dubbo内置的负载均衡策略：

- 随机（random）
- 轮询（roundrobin）
- 最少活跃调用数（leastactive）
- 一致性hash（consistenthash）
- shortestresponse

默认为随机。



dubbo负载均衡策略既可以在消费端进行配置，也可以在服务端进行配置，可以配置服务级别，也可以配置方法级别。



服务端服务级别配置示例：

```
<dubbo:service interface="com.liu.service.HelloService" ref="helloService" loadbalance="roundrobin"/>
```

服务端方法级别配置：

```
<dubbo:service interface="com.liu.service.HelloService" ref="helloService" >
    <dubbo:method name="sayHello" loadbalance="random" />
  </dubbo:service>
```

客户端服务级别配置：

```
<dubbo:reference cache="true" id ="helloService" interface="com.liu.service.HelloService" loadbalance="random"/>
```

客户端方法级别配置：

```
<dubbo:reference id ="helloService" interface="com.liu.service.HelloService" loadbalance="random">
<dubbo:method name="sayHello" loadbalance="random"/>
</dubbo:reference>
```





除了使用内置的负载均衡策略外，dubbo还支持自定义负载均衡策略。



下面我们就来实现一个自定义负载均衡器，会对IP和端口号进行排序，并选择第一台节点进行调用。



自定义负载均衡策略实现步骤：

- 定义一个自定义负载均衡器，导入`dubbo`坐标

```
  <dependency>
          <groupId>org.apache.dubbo</groupId>
          <artifactId>dubbo</artifactId>
      </dependency>
```

  

- 实现`LoadBalance`接口，并重写`select`方法 

```
  public class OnlyFirstloadbalance implements LoadBalance {
      @Override
      public <T> Invoker<T> select(List<Invoker<T>> list, URL url, Invocation invocation) throws RpcException {
          //按照ip、port排序后，选择第一个
          return list.stream().sorted((i1, i2) -> {
              int ipCompare = i1.getUrl().getIp().compareTo(i2.getUrl().getIp());
              if(ipCompare == 0) {
                  return Integer.compare(i1.getUrl().getPort(), i2.getUrl().getPort());
              }
              return ipCompare;
          }).findFirst().get();
      }
  }
```

- 使用SPI机制配置

```
  onlyfirstBalance=com.liu.OnlyFirstloadbalance
```

- 在其他项目中引入即可。

  

### Dubbo过滤器



Dubbo提供了过滤器功能，通过该机制能在执行目标前后执行我们自己制定的代码逻辑。



可以实现的功能：

- IP白名单限制
- 监控
- 日志记录



Dubbo过滤器实现步骤：

- 自定义过滤器类，实现` org.apache.dubbo.rpc.Filter `接口；

```
  import org.apache.dubbo.common.constants.CommonConstants;
  import org.apache.dubbo.common.extension.Activate;
  import org.apache.dubbo.rpc.*;
  import org.apache.dubbo.rpc.Filter;
  
  @Activate(group = {CommonConstants.PROVIDER})
  public class LogFilter implements Filter {
      @Override
      public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
          Long start = System.currentTimeMillis();
          try {
              System.out.println("拦截到啦");
              return invoker.invoke(invocation);
          } finally {
              Long end = System.currentTimeMillis();
              System.out.println("耗时：" + (end - start));
          }
      }
  }
```

  使用` org.apache.dubbo.common.extension.Activate `接口进行对类进行注册 通过group 可以 指定生产端 消费端

- 在reources/META-INF/dubbo目录下新建一个文件，文件名为`org.apache.dubbo.rpc.Filter`，写入`LogFilter`类的全限定名

- 在需要进行拦截的项目中引入spi依赖，启动后即可生效

```
  <dependency>
        <groupId>com.liu</groupId>
        <artifactId>dubbo-filter</artifactId>
        <version>1.0-SNAPSHOT</version>
      </dependency>
```



一般情况下，这种非业务功能都会单独进行开发，然后使用SPI机制进行引入。



### Dubbo异步调用



Dubbo默认为同步阻塞调用，也可以自定义实现异步调用，异步调用会返回一个 Future 对 象。



- 消费方指定异步调用：

```
<dubbo:reference id ="helloService" interface="com.liu.service.HelloService" loadbalance="random">
    <dubbo:method name="sayHello" async="true" loadbalance="random"/>
</dubbo:reference>
```

async为true即表示异步调用

- 异步调用并获取返回值：

```
Future<Object> future = RpcContext.getContext().getFuture();
Object o = future.get();
System.out.println(o);
```

异步调用需要通过`Future`来获取异步返回值。



### Dubbo线程池

Dubbo中的线程池模型：

配置：

```\
<dubbo:protocol name="dubbo" port="20885" threadpool="limited"/>
```



- `fixed`：固定大小线程池，启动时创建线程，不关闭，一直持有。默认线程池类型，默认为创建200个线程。
- `cached`：非固定大小线程池，当线程不足时，会自动创建
- `limited` 可伸缩线程池，但池中的线程数只会增长不会收缩。只增长不收缩的目的是为了避免收缩时突然来了大流量引起的性能问题。
- `eager` 优先创建`Worker`线程池.



Dispatcher

- `all` 所有消息都派发到线程池，包括请求，响应，连接事件，断开事件，心跳等。

- `direct` 所有消息都不派发到线程池，全部在 IO 线程上直接执行。

- `message` 只有请求响应消息派发到线程池，其它连接断开事件，心跳等消息，直接在 IO 线程上执行。

- `execution` 只有请求消息派发到线程池，不含响应，响应和其它连接断开事件，心跳等消息，直接在 IO 线程上执行。

- `connection` 在 IO 线程上，将连接断开事件放入队列，有序逐个执行，其它消息派发到线程池。

  

自定义线程池实现线程池监控，每隔三秒打印线程数量，如果使用的线程数量超过总线程数的90%，则报警。

- 定义线程监控代码：

```
  public class WatchingThreadPool extends FixedThreadPool implements Runnable {
  
      private static final Logger LOGGER = LoggerFactory.getLogger(WatchingThreadPool.class);
  
      private static final  double ALARM_PERCENT = 0.90;
  
      private final Map<URL, ThreadPoolExecutor> THREAD_POOLS = new ConcurrentHashMap<>();
  
      public WatchingThreadPool() {
          //每隔3秒打印线程使用情况
          Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this, 1, 3, TimeUnit.SECONDS);
      }
  
      @Override
      public Executor getExecutor(URL url) {
  
          //从父类中创建线程池
          Executor executor = super.getExecutor(url);
          if(executor instanceof ThreadPoolExecutor) {
              THREAD_POOLS.put(url, (ThreadPoolExecutor) executor);
          }
          return executor;
      }
  
      @Override
      public void run() {
          //遍历线程池，如果超出指定的部分，进行操作， 比如发端短信等
          for (Map.Entry<URL, ThreadPoolExecutor> entry : THREAD_POOLS.entrySet()) {
              URL key = entry.getKey();
              ThreadPoolExecutor executor = entry.getValue();
  
              //当前执行中的线程数
              int activeCount = executor.getActiveCount();
              //总线程数
              int corePoolSize = executor.getCorePoolSize();
  
              double used = (double) activeCount / corePoolSize;
              int usedNum = (int) (used * 100);
              LOGGER.info("线程池执行状态：[{}/{}]:{}%", activeCount, corePoolSize, usedNum);
  
              if(used >= ALARM_PERCENT) {
                  LOGGER.error("超出警戒值！host:{}，当前已使用量：{}%, URL:{}", key.getIp(), usedNum, key);
              }
          }
      }
  }
```

  在该类中，继承dubbo的`FixedThreadPool`，从而实现fixed固定线程池的扩展，并实现了`Runnable`接口， 每隔三秒钟获取到当前执行的线程数和总线程数的比例，超过一定的阈值则打印出警戒。

- 注册为SPI：在`resources/META-INF/dubbo`下创建文件`org.apache.dubbo.common.threadpool.ThreadPool`，并将`WatchingThreadPool`全限定名写入文件中，起名为`watching`：

```
  watching=com.liu.thread.WatchingThreadPool
```

  

- 在服务提供方引入SPI依赖，并修改其实现，模拟接口延时

```
  @Override
    public String sayHello(String name) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return "hello2 : " + name;
    }
```

- 将服务提供方的线程池类型改为自定义的线程池类型

```
    <dubbo:protocol name="dubbo" port="20885" threadpool="watching"/>
```

  

- 为了模拟并发情况，在消费方创建大量线程去调用接口

```
  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
      ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:dubbo-consumer.xml");
      context.start();
      HelloService bean = context.getBean(HelloService.class);
      while (true) {
        for (int i = 0; i < 1000; i++) {
          Thread.sleep(5);
          new Thread(new Runnable() {
            @Override
            public void run() {
              String liujiao = bean.sayHello("liujiao");
              System.out.println(liujiao);
            }
          }).start();
        }
      }
    }
```

  

- 启动服务提供者以及消费者，可以在提供者控制台看到打印的线程占用情况，当占用量达到设定阈值后，控制台打印超出警戒的提示

```
  20:04:53.836 [pool-5-thread-1] INFO  com.liu.thread.WatchingThreadPool.run(WatchingThreadPool.java:49) - 线程池执行状态：[166/200]:83%
  20:04:56.835 [pool-5-thread-1] INFO  com.liu.thread.WatchingThreadPool.run(WatchingThreadPool.java:49) - 线程池执行状态：[164/200]:82%
  20:04:59.834 [pool-5-thread-1] INFO  com.liu.thread.WatchingThreadPool.run(WatchingThreadPool.java:49) - 线程池执行状态：[166/200]:83%
  20:05:02.833 [pool-5-thread-1] INFO  com.liu.thread.WatchingThreadPool.run(WatchingThreadPool.java:49) - 线程池执行状态：[158/200]:79%
  20:05:05.832 [pool-5-thread-1] INFO  com.liu.thread.WatchingThreadPool.run(WatchingThreadPool.java:49) - 线程池执行状态：[156/200]:78%
```

  

### Dubbo服务降级



1. 屏蔽，不进行调用，直接返回null或者其他值

```
<dubbo:consumer  timeout="1000" mock="force:return null" retries="3"/>
```

2. 调用失败时，返回null或者指定值

 ```
   <dubbo:consumer  timeout="1000" mock="fail:return 失败啦" retries="3"/>
 ```

3. 自定义返回值：

   将mock设置为true：

 ```
   <dubbo:consumer  timeout="1000" mock="true" retries="3"/>
 ```

   并自定义类来处理超时：

 ```
   public class HelloServiceMock implements HelloService{
       @Override
       public String sayHello(String name) {
           return "不好意思，请求超时啦";
       }
   }
 ```

   这里需要注意：HelloServiceMock的名字为dubbo:refrence中的接口名加+Mock



### Dubbo调用结果返回值缓存



```
<dubbo:reference cache="true" />
```





### Dubbo集群容错方案



- 失败自动切换自动重试其他机器（默认）
- 快速失败，立即报错
- 失败安全，出现异常时，直接忽略
- 失败自动恢复，记录失败请求，定时重发
- 并行调用多个服务器，只要成功一个即可返回
- 广播逐个调用所有提供者，任意一个报错则报错



代码地址：https://github.com/liujiao111/dubbodemo