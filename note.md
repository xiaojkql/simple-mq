搜索过的技术

[Netty 参数设置](https://blog.csdn.net/zhongzunfa/article/details/94590670)

[keep-alive]
[SO_KEEPALIVE选项](https://www.cnblogs.com/tekkaman/p/4849767.html)
[TCP中已有SO_KEEPALIVE选项，为什么还要在应用层加入心跳包机制?](https://www.zhihu.com/question/40602902)

[SO_BACKLOG]
[java socket参数详解:BackLog](https://blog.csdn.net/huang_xw/article/details/7338487?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-4.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-4.control)
[Netty ChannelOption.SO_BACKLOG参数详解](https://blog.csdn.net/fd2025/article/details/79740226?utm_medium=distribute.pc_relevant.none-task-blog-baidujs_title-2&spm=1001.2101.3001.4242)


[SO_REUSEADDR]
[Socket中SO_REUSEADDR详解](https://blog.csdn.net/u010144805/article/details/78579528)


[TCP_NODELAY]
[TCP连接中启用和禁用TCP_NODELAY有什么影响？](https://blog.csdn.net/lclwjl/article/details/80154565)

关键点：
粘包/拆包问题 
LengthFieldBasedFrameDecoder的工作机制，运行时怎样进行解析

怎样进行编码和解码的

心跳Handler IdleStateHandler的工作机制

共享内存池工作机制


细节的处理
- channel通道的维护



异步/同步发送消息的具体实现
