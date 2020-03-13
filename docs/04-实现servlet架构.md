
## 猜一个servlet的架构
我们提起java的服务端，基本上必提servlet，服务容器,那么servlet究竟是什么?
在J2EE 7的文档中,Servlet解释如下:
>Java Servlet technology lets you define HTTP-specific servlet classes. A servlet class
 extends the capabilities of servers that host applications accessed by way of a
 request-response programming model. Although servlets can respond to any type of
 request, they are commonly used to extend the applications hosted by web servers.

译成中主语言大意如下（原谅笔者的蹩脚的翻译...):

> Java Servlet技术可以让你定义面向Http服务的类,在运行基于request-respone的Http应用的服务器上，
servlet技术实现的class扩展了服务的能力，虽然servlet的技术可以应用很多场景,但它更多还是用于web服务器上。

再参考一些tomcat等webserver的文档，我们得出结论如下（**这部分表述可能不准确,期待高人指点**)

1. servlet是一组web服务类的标准(即接口的集合？)
2. tomcat 等都是对这种标准的类实现了服务的支持,即如果类实现了这种接口,我们就可以对这种类对支持。

并且因为servlet是j2ee的标准，也意味着它是j2ee的一个标准包,BUT:

**在和几个程序员讨论后,我准备不依赖任何maven包搞定这个工程,所以,我决定接着把基本接口抄过来**

> 如果看完上一句，你还没有关闭本文章，拉黑本公众号,本拐先谢了您。

本着尽可能的简单,直观的原则 
我们目前`copy`了如下的定义:
```bash
|——GenericServlet
    |——HttpServlet
|——ServletRequest
    |——HttpServletRequest
|——ServletResponse
    |——HttpServletRespone
```
嗯，复制完代码名称以后，突然觉得,似乎也没那么疯狂.

从类的命名我们可以看出Servlet编程的基本结构了。
1. Servlet是创建一次,反复运行（这点可以参照文档的第五篇）
2. 显然,在创建一次以后,每次运行时会以`ServletRequsest`和`ServletResponse`作为参数传入到运行方法中。分别表示这次的请求和返回。
3. 所以,GenerictServlet**必然有一个方法**,用来表示每次的运行,这个方法,在javax的规范里,就是`service(ServletRequest req,ServletRespone res)`
4. 因为Servlet技术不只面向于Http,所以，有了一组HttpServlet*的定义。
5. 从规范的角度讲,到HttpServlet的这一层,应该对`service`操作进行进一步的细化,所以在`HttpServlet`中,实现了`service`，但是抽象出`doGet`,`doPost`等一系列方法交由具体的业务类去实现。
6. 同时,因为有了servlet的定义，使得服务应用程序可以只加载已经实现的servlet，而不必去关注其实现的细节，于是有了tomcat/jetty这种web容器。

这部分代码本拐作了一些剪裁,在工程中就不进行细讲了:)

> 值得注意的是,观察其他语言的一些框架,比如node的express，或是php的lumen,大部分的业务代码是在`service`这一层实现的业务逻辑，而java借助于servlet对业务进了进一步的封装,值得我们在业务实现时可以直接以接收反序列化后的request,处理后直接返回相应的结果,极大的方便了工程师,这或许也是java在企业领域能够流行的重点之一。

1. [JavaWeb 开发之Servlet的体系结构](https://blog.csdn.net/z_x_qiang/article/details/88142493)
2. [HttpServlet官方](https://docs.oracle.com/javaee/8/api/javax/servlet/http/HttpServlet.html)
4. [J2EE 7 官方文档](https://docs.oracle.com/javaee/7/index.html)
5. [Servlet简介](https://www.runoob.com/servlet/servlet-intro.html)
6. [Servlet的github](https://github.com/GADNT/javax.servlet)