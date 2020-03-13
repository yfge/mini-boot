
## 序
本篇是《跟我猜Spring-Boot》系列第四篇，至于为什么有这个，前面已经啰嗦太多，不作缀述。
本篇文章所有代码的改动都基于

上一篇，我们已经实现了基本的HttpServer，但是大家也看到了，功能虽然实现了，但是代码很丑，主要因为：

> 1. `Application.loadBean`太复杂,太长了,重复的代码很多！
> 2. 我已经有了ApplicationContext来管理Bean,为什么还要把bean包裹在UrlMap里一层一层传递？
> 3. ServerRunnable那个类实现的实在丑了（丑出天际！！）有没有办法优雅一点？
>
那么，在继续给我们的mini-boot增加功能之前，我们先把这几个问题解决一下，免得以后的坑越来越多。

那么针对上面的1，2，3 咱们开始一样一样的梳理。

## 优化`Application.loadBean`

## 处理`ApplicationContext`

## 处理`ServerRunnable`









## 参考
[sring-annotation-how-it-works](https://dzone.com/articles/spring-annotation-processing-how-it-works)
[JSR 250:Common Annotions for JavaPlatform](https://jcp.org/en/jsr/detail?id=250)