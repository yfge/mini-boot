# 基本的HttpServer
## 引＆目标
本篇是《跟猜Spring-boot》系列文章的第三篇,本拐做事向来随机随意,三分热血，有始无终,希望《猜》系列文章可以成为本拐第一个正常完结的系列。

在这个系列里，笔者将会通过简单实现Spring里各种功能和特性，试着拆解这些功能特性背后的实现机理。

我们最终靠“猜"实现的源码，肯定会与真正的工程化的东西差十万八千里。

但是通过这些猜出来的源码，希望各位与笔者一样，对设计模式,语言特性会有一些深层的认知。


废话不多说（明明已经说了那么。。。），在上一篇里，我们已经实现了简单的bean注入。

那么我们这一篇，将去实现一个简单的HttpServer. 
这其中，所有的源码的改造都基于：
`https://github.com/yfge/mini-boot/archive/'article-02'.tar.gz`

既然要实现HttpServer,对照Spring的一些功能,我们需要标注Controller以达到:

1. 说明它是一个restController
2. 标注地址
3. 标注方法

这样，改造后的`SimpleController`如下:

**SimpleController.java**
```java
package com.github.yfge.miniapp;
//省去import ..
@RestController
@RequestMapping(path="/")
public class SimpleController {
    @Autowired
    private SimpleService simpleService;

    public SimpleController(){
        System.out.println("the controller is created!");
    }
    @PostConstruct
    public void init(){
        System.out.println("the service Id is :"+this.simpleService.getServiceId());
    }

    @RequestMapping(path="/hello",method = RequestMethod.GET)
    public String getHello(){
        return simpleService.getHelloMessage();
    }
}
```

那么对应的，我们需要在`SimpleService`上加一个`getHelloMessage()`的实现:

```java
package com.github.yfge.miniapp;
//略去import
@Service
public class SimpleService {

    private String serviceId ;
    public SimpleService(){
        serviceId= UUID.randomUUID().toString();
    }
    public String getServiceId(){
        return this.serviceId;
    }
    public String getHelloMessage(){
        StringBuilder builder = new StringBuilder();
        builder.append("Hello ,the Service is :")
                .append(this.serviceId);
        return builder.toString();
    }
}
```
## 需求分析
由我们的改造目标可以看到，我们需要做到：

1. 实现一个简单的HttpServer
2. 定义我们需要实现的注解(`RequestMapping`,`RespnseBody`,`RestController`)并实现挂载逻辑。

## 实现简单的HttpServer
JDK本身已经有了一个HttpServer，使用起来非常简单粗暴，类似于这个样子:

```java
        try {
            server = HttpServer.create(new InetSocketAddress(8080),0);
            server.createContext("/",new ServerHandler());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
```
为了让这个server与我们的简单框架结合到一起，我们显然要把它加到`Application.loadBeans`结尾。

在这段代码里,`ServerHandler`是一个`HttpHandler`简单实现,类似于下面这样:

```java
 public class ServerHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "hello world";
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
```

在这个简单实现里，我们已经能看到一个HttpServer的基本的结构了,但有一个问题，就是这个HttpServer其实是单线程的。
为了验证个问题，可以加一个sleep来测试,这里就不再缀述了。

为了能让他并发执行，我们简单的加一个并发的机制。
即引入一个`ThreadPool`来进行执行请求,同时将原有的处理逻辑抽象成`ServiceRunnable`的类。
`ServiceRunnable`这个类名字听起来或许**有些不那么靠谱**,但是我们目前为止似乎只看到了这些,所以,更改后的`ServerHandler`变成了下面的样子:

**ServerHandler.java**
```java
public class ServerHandler implements HttpHandler {
    private ThreadPoolExecutor poolExecutor ;
    public ServerHandler(){
        poolExecutor= (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        poolExecutor.execute(new ServiceRunnable(exchange));
    }

}
```
我们这个简单的`ServiceRunnable`就成了如下样子:
**ServiceRunnable.java**
```java

public class ServiceRunnable implements Runnable {
    private final HttpExchange exchange;
    public ServiceRunnable(HttpExchange exchange) {
        this.exchange = exchange;
    }
    @Override
    public void run() {
        try {
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("hello".getBytes());
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```
OK,做了以上的改动，我们已经实现了一个简单的HttpServer,虽然它目前只会输出一个Hello,但是通过我们的一些设计，他实现了:
1. 端口监听和服务启动
2. 我们将处理请求的逻辑移到了`ServiceRunnable`这个类中,意味着对Http的一些扩展性操作我们只要和`ServiceRunnable`打交道就可以了。
## 定义注解
在实现了HttpServer以后,我们需要照着Spring的方式定义一系列的注解:

**RequestMapping.java**
```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface RequestMapping {
    String[] path() default {};
    RequestMethod[] method() default {};
}
```
**ResponseBody.java**
```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface ResponseBody {
}
```
**RestController.java**
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface RestController {
}
```
那么像之前一样，我们需要将这些注解解析,并得创建相应的bean.
这里面，重要的一点,我们的`SimpleController`已经没有Service的注解,而只有了RestController这个注解。
并且为了标明RestController也是一个bean，也要创建，我们在`RestController`上加了`@Service`
这就意味着我们的Bean创建逻辑要更改,即，从创建是否含有`@Service`的注解,到循环判断这个类的**注解上是否也有@Service注解**!
有一些绕是吧，其实我们只是需要对`Application.loadBean`方法中下面这段做更改:
```java
  for (String name : classNames) {
            try {
                var classInfo = Class.forName(name);
                /**
                 * 检查是否声明了@Service
                 **/
                if (classInfo.getDeclaredAnnotation(Service.class) != null) { // 这里变得不适用了。
                    /**
                     * 得到默认构造函数
                     */
                    var constructor = classInfo.getConstructor();
                    if (constructor != null) {
                        /**
                         * 创建实例
                         */
                        var obj = constructor.newInstance();
                        /** 保存bean**/
                        applicationContext.addBean(obj);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

```
将其中的判断抽取成一个方法，然后进行调用，如下:
```java

public class Application {


    /**
     * 检查一个类是否需要创建（是否是bean)
     * @param beanClass
     * @return
     */
    private static boolean isNeedToCreate(Class beanClass) {
        var annotations = beanClass.getDeclaredAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (var annotation : annotations) {
                if (annotation.annotationType() == Service.class) {
                    return true;
                } else {
                    if(annotation.annotationType()!= Target.class && annotation.annotationType()!= Retention.class) {
                        return isNeedToCreate(annotation.annotationType());
                    }
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 加载相应的bean(Service)
     *
     * @param source
     */
    private static void LoadBeans(Class source) {
        ClassUtils util = new ClassUtils();
        List<String> classNames = util.loadClass(source);
        /** 实例化一个context **/
        ApplicationContext applicationContext = new ApplicationContext();
        for (String name : classNames) {
            try {
                var classInfo = Class.forName(name);
                /**
                 * 检查是否声明了@Service
                 **/
                if (classInfo.isAnnotation() == false && isNeedToCreate(classInfo)) {
                    /**
                     * 得到默认构造函数
                     */
                    var constructor = classInfo.getConstructor();
                    if (constructor != null) {
                        /**
                         * 创建实例
                         */
                        var obj = constructor.newInstance();
                        /** 保存bean**/
                        applicationContext.addBean(obj);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    // other code
    }
}
```
在对我们的bean进行优化以后,我们要真正开始进行地址的映射了，那么根据之前的经验,我们要解决的问题有:
1. 将标有`@RestController`的Bean取出。
2. 对所有相应的Bean进行遍历,根据类的注解的RequestMapping和方法上的RequestMapping,进行映射及组装地址。
3. 将地址和对应的方法绑定到`ServerHandler`中。

OK 按照我们的思路,第一步很容易，第二步呢，我们需要一个Map来进行存储。 这个Map应该是一个K-V结构。
即`URL-HTTP方法-类方法`
这里面,为了方便管理，我们直接将三类信息封装成一个类`UrlMappingInfo`,同时,为了保证方法可以正常的调用,我们需要把对应的bean也传入。

***UrlMappingInfo**
```java
public class UrlMappingInfo {
    private String url;
    private RequestMethod[] requestMethods;
    private Method method;
    private Object bean;
    public UrlMappingInfo(String url,RequestMethod[] requestMethods ,Method method,Object bean){
        this.url=url;
        this.requestMethods = requestMethods;
        this.method = method;
        this.bean=bean;
    }

    public RequestMethod[] getRequestMethods() {
        return requestMethods;
    }
    public Method getMethod(){
        return method;
    }
    public String getUrl(){
        return url;
    }
    public Object getBean(){
        return bean;
    }
}

```

然后实现地址的映射,并且将这个结构传入到我们的ServerHandler中。
***Application.java**
```java


public class Application {
  /**
     * 加载相应的bean(Service)
     *
     * @param source
     */
    private static void LoadBeans(Class source) {
        // other code
        Map<String ,UrlMappingInfo> urlMappingInfoMap = new LinkedHashMap<>();
        /** 实例化一个context **/
        /** 注入bean **/
        /** 执行初始化方法 **/
        /** 生成地址映射**/
        for(Object ob :applicationContext.getAllBeans()){
            var classInfo = ob.getClass();
            if(classInfo.getDeclaredAnnotation(RestController.class)!=null){
                var requestMapping = classInfo.getDeclaredAnnotation(RequestMapping.class);
                String[] baseUrl={};
                if(requestMapping!=null){
                    baseUrl = requestMapping.path();
                }
                for(var method:classInfo.getDeclaredMethods()){
                    var methodRequestMapping = method.getDeclaredAnnotation(RequestMapping.class);
                    if(methodRequestMapping!=null){
                        String[] subUrl = methodRequestMapping.path();
                        for (var base: baseUrl
                             ) {
                            for(var sub:subUrl){
                                String url = base+sub;
                                urlMappingInfoMap.put(url,new UrlMappingInfo(url,methodRequestMapping.method(), method,ob));
                            }
                        }
                    }
                }
            }
        }
  //
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new ServerHandler(urlMappingInfoMap));
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
```

**ServerHandler.java**
```java
public class ServerHandler implements HttpHandler {
    private final Map<String, UrlMappingInfo> urlMappingInfoMap;
    private ThreadPoolExecutor poolExecutor ;
    public ServerHandler(Map<String, UrlMappingInfo> urlMappingInfoMap){
        this.urlMappingInfoMap = urlMappingInfoMap;
        poolExecutor= (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        poolExecutor.execute(new ServerRunnable(exchange,urlMappingInfoMap));
    }
}
```

可以看到,这里,我们已经同时将urlMappingInfoMap这个结构传到了ServerRunnable中,那么更改ServerRunnable就变得简单起来。
**ServerRunnable.java**
```java

public class ServerRunnable implements Runnable {

    private final HttpExchange exchange;
    private final Map<String, UrlMappingInfo> urlMappingInfoMap;

    public ServerRunnable(HttpExchange exchange, Map<String, UrlMappingInfo> urlMappingInfoMap) {
        this.urlMappingInfoMap = urlMappingInfoMap;
        this.exchange = exchange;
    }

    @Override
    public void run() {
        try {
            String url = this.exchange.getRequestURI().getPath();
            String method = this.exchange.getRequestMethod();
            System.out.println(method + " " + url);

            try {
                var mappingInfo = this.urlMappingInfoMap.getOrDefault(url, null);
                if (mappingInfo == null) {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().write("Error Not Found.".getBytes());
                } else {
                    boolean isAllowed = false;
                    for (var allowedMethod : mappingInfo.getRequestMethods()) {
                        if (allowedMethod.toString().equals(method)) {
                            var ob = mappingInfo.getMethod().invoke(mappingInfo.getBean());
                            exchange.sendResponseHeaders(200, 0);
                            exchange.getResponseBody().write(ob.toString().getBytes());
                            isAllowed=true;
                            break;
                        }
                    }
                    if(isAllowed ==false){
                        exchange.sendResponseHeaders(405,0);
                        exchange.getResponseBody().write("not allowed!".getBytes());
                    }
                }
            }catch (Throwable e){
                exchange.sendResponseHeaders(500,0);
                exchange.getResponseBody().write("internal error".getBytes());
            }
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

现在编译运行整个程序. 你可以看到如下输出:
```bash
the controller is created!
the service :fa68fc94-933b-4ae6-b65b-4f2814dad76a is created!
/hello is Mapping
The Mini-Boot Application Is Run! The Name is Hello
```

做一些测试
```bash
curl localhost:8080/hello
Hello ,the Service is :fa68fc94-933b-4ae6-b65b-4f2814dad76a

curl localhost:8080/hello1
Error Not Found.

curl -X POST localhost:8080/hello
not allowed!

```
到现在为止，我们已经：

1. 实现了简单的HttpServer
2. 成功模拟了RestController和RequestMapping的功能

但是,我们代码,总是感觉有一些不对劲？
1. `Application.loadBean`太复杂,太长了,重复的代码很多！
2. 我已经有了ApplicationContext来管理Bean,为什么还要把bean包裹在UrlMap里一层一层传递？
3. ServerRunnable那个类实现的实在丑了（丑出天际！！）有没有办法优雅一点？

OK，那么针对这两个问题，我们将在下一篇文章,对代码进行第一次重构。

## 其他
> 不给源码的分享都是耍流氓！

所以，我们的项目地址是：
https://github.com/yfge/mini-boot
由于，随着文章的发布，本代码会不停的更新，所以，本章的tag是**:`article-03`**(原谅我起名字的水平)
> 本来计划今天直接把servlet撸定。。但是似乎。。有点深，所以servlet的东西会单独放到一篇去写。
>另外，由于面向功能的代码写起来实在是太丑了，所以准备先单独写一篇文档，去重构一些东西，以理清一些思路，算是我自己的一个整理吧。
>希望对您也是有用的。
## 参考

3. [使用Java内置的Http Server构建Web应用](https://www.cnblogs.com/aspwebchh/p/8300945.html)
: