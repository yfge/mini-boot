# bean的创建

## 废话在前

最近几年的技术路子很杂，先是node,然后是php，后来是openresty,再后来转到了java，而接触的框架（Framework），也越发的复杂，从最开始的express/koa,到lumen ,再到spring全家桶，各种切换，也确实一度头疼；
一般而言，学习一种技术下的某种框架当然是去读源码，但开源项目动辙几万十几万行，尤其我又是个懒人，实在是没办法分析了解。于是，我选择了另一种方式，即在使用框架过程中去“猜想“，即去思考他应该去怎么实验的；
其实，不停的猜想，然后写小demo去实现，再到框架源码中针对性的找到机制去阅读，也不失为一种乐趣。
出于这种思维，我在工作中不停的接触过往同事的源码,再从源码中学习到spring各种特性的用法，再去猜想，实现，查看代码去验证，也确实为工作添了些许乐趣。出于这种目的，想把自己这些的猜想做一些整理，与大家分享。

本系列的文章预谋好久，然而一直不知道该如何开始，不如写一篇算一篇。在这些文章中，我将先去写一个spring的应用特性，然后靠“猜想“去将背后的特性实现出来。
那么本篇，从最基本的入口程序开始吧。
从这个入口程序，我们将看到一个spring bean创建的简单机制。

## 目标

在本篇文章中，我们要实现一个spring的入口程序，即：

**App.java**
```java
package com.github.yfge.miniapp;
import com.github.yfge.miniboot.autoconfigure.BootApplication;
import com.github.yfge.miniboot.autoconfigure.Application;

@BootApplication(Name = "Hello")
public class App {
    public static void main(String[] args) {
        Application.run(com.github.yfge.miniapp.App.class);
    }
}
```

同时，定义一个简单的`controller`和`service`:

**SimpleController.java**

```java
package com.github.yfge.miniapp;

import com.github.yfge.miniboot.autoconfigure.Autowired;
import com.github.yfge.miniboot.autoconfigure.Service;

@Service
public class SimpleController {
    @Autowired
    private SimpleService simpleService;
    public SimpleController(){
        System.out.println("the controller is created!");
    }
}
```

**SimpleService.java**
```java
package com.github.yfge.miniapp;
import com.github.yfge.miniboot.autoconfigure.Service;
@Service
public class SimpleService {
    public SimpleService(){
        System.out.println("the service is created!");
    }
}
```
即我们要通过这个入口程序的运行，看到`service`和`controller`被**自动**的创建。

通过这三个文件的import也看到了，**我们没有引用spring-boot**，而是自己写了一些简单的类引用进来，来**模拟**spring-boot

在这种情况下，我们整个项目很自然分成两个module:
* mini-app，用来模拟我们的应用
* mini-boot,用来模拟spring-boot框架

## bean创建的需求分析
> sorry 我实在想不来有什么比需求分析更适合这节标题 :)

如果我们要达到上述的目标，那我们的小框架应该实现如下的功能：

1. 定义相应的annotation
2. 自动扫描相应的类
3. 如果类被标明是service，那么它应该被自动创建

那么下面，就按这三步来实现我们的目标。

## step 1 定义相应的anotation

从我们的目标程序，可以看到，我们需要有`BootApplication`和`Service`两个annotation

既然没有引用spring-boot,那么只能自己动手了
这里为了与spring-boot对齐 （因为我们是模拟么），直接照搬命名空间和类名。

**BootApplication.java**
```java
package com.github.yfge.miniboot.autoconfigure;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BootApplication {
     String Value() default "";
     String Name() default "";
}
```

**Service.java**

```java 
package com.github.yfge.miniboot.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    String value() default "";
}
```

这里要注意一点，**@Retention(RetentionPolicy.RUNTIME)** 这个注解非常重要，因为只有定义为运行时，我们在程序运行的时候才有可能被扫描到。

## Step3 实现包的类扫描

关于包的类扫描，网上可以到到N种实现，这里不表述了，直接copy一个现成的 （是的，我们都是代码的搬运工😊）
```java
package com.github.yfge.miniboot.autoconfigure;
import java.net.JarURLConnection;
import  java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {
    public List<String> loadClass(Class t){
        String packageName = t.getPackage().getName();
        return loadClass(packageName);
    }
    private List<String> loadClass(String packageName) {
        String packagePath = packageName.replace('.','/');
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        URL url = loader.getResource(packagePath);
        if (url!=null){
            String protocol = url .getProtocol();
            if (protocol.equals("file")){
                return getClassFromDir(url);
            }else if (protocol.equals("jar")){
                return getClassFromJar(url);
            }
        }else {
            System.out.println("loader fail.");
            return null;
        }
        return  null;
    }
    private List<String> getClassFromJar(URL url){
        try {
            JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            List<String> classNames = new ArrayList<>();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() == false) {
                    String entryName = entry.getName().replace('/','.');
                    if(entryName.endsWith(".class") && entryName.contains("$")==false ) {
                        classNames.add(entryName.replace(".class",""));
                    }
                }
            }
            return classNames;
        }catch (java.io.IOException ex){
            return null;
        }
    }
    private List<String> getClassFromDir(URL url ){
        return null;
    }

}
```

## Step3 实现bean的创建

OK，现在我们已经把所需的工具都准备好了，现在开始实现最核心的bean的创建，即`Application.run`的入口,目前，它非常简单：

```java
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Application {
    /**
    * other code ..
    **/

    public static void run(Class source) {
        BootApplication a = (BootApplication) source.getAnnotation(BootApplication.class);
        LoadBeans(source);
        System.out.println("The Mini-Boot Application Is Run! the Name is "+a.Name());

    }
}
```

可以看到，它的功能就是加载所有的bean，然后输出程序已经启动的信息。加载bean的实现是`loadBeans` ，我们之前已经准备好了`classUtils`,所以这个`loadBeans`只需要实现两个功能:

1. 用classUtils得到所有的类名
2. 如果这个类被标注为service ,那就创建它。

故而代码如下:

```java
/**
 * 加载相应的bean(Service)
 *
 * @param source
 */
private static void LoadBeans(Class source) {
    ClassUtils util = new ClassUtils();
    List<String> classNames = util.loadClass(source);
    for (String name : classNames) {
        try {
            var classInfo = Class.forName(name);
            /**
            * 检查是否声明了@Service
             **/
            if (classInfo.getDeclaredAnnotation(Service.class) != null) {
                /**
                 * 得到默认构造函数
                 */
                var constructor = classInfo.getConstructor();
                if (constructor != null) {
                    /**
                     * 创建实例
                     */
                    var obj = constructor.newInstance();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
```

好了，编译整个程序，你会看到输出如下：
```bash
the controller is created!
the service is created!
The Mini-Boot Application Is Run! The Name is Hello
```

到此，我们的目标已经完成 :)

当然，这个程序目前还有很多问题,或是说不足:

1. 只实现了service的创建，没有实现注入;
2. 创建时只支持默认的无参数的构造函数;
3. 类的结构也不尽合理.

这就引出了本系列(如果它能成为一个系列的话....)的写作原则，即：

我们每次只用最粗暴的方式实现我们想要的功能，在接下来的文章中（如果我能坚持下来…），会不停的往这个小程序上加上我们想要的功能，如果代码结构不合理,我们再去抽象，封装，更改现有实现，直到它一点一点的完善，小而美的可用，即，
> 不会为了设计而设计，亦不会为了架构而架构

## 其他
> 不给源码的分享都是耍流氓！

所以，我们的项目地址是：
https://github.com/yfge/mini-boot
由于，随着文章的发布，本代码会不停的更新，所以，本章的tag是`article-01`(原谅我起名字的水平)