package com.xiaojkql.simplemq.remote.netty;

import com.xiaojkql.simplemq.remote.RemotingServer;
import com.xiaojkql.simplemq.remote.common.Pair;
import com.xiaojkql.simplemq.remote.protocol.RemotingCommand;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
public class NettyRemotingServer extends NettyRemotingAbstract implements RemotingServer {

    private final ExecutorService publicExecutor;

    private final NettyServerConfig nettyServerConfig;
    private final ServerBootstrap serverBootstrap;
    // 用于serverChannel accept
    private final EventLoopGroup eventLoopGroupSelector;
    // 用于socketChannel 轮询
    private final EventLoopGroup eventLoopGroupBoss;
    // 编码Handler
    private NettyEncoder nettyEncoder;
    // 服务端业务handler
    private NettyServerHandler nettyServerHandler;
    // Netty SocketChannel各个事件处理线程池
    private DefaultEventExecutorGroup defaultEventExecutor;


    public NettyRemotingServer(NettyServerConfig nettyServerConfig) {
        // 【创建bootStrap】
        // 【创建线程池】
        this.nettyServerConfig = nettyServerConfig;
        this.serverBootstrap = new ServerBootstrap();

        final int publicThreadNums = nettyServerConfig.getPublicThreadNums();

        this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("public-executor-%d-%d", publicThreadNums, threadIndex.getAndIncrement()));
            }
        });

        final int eventLoopGroupBossThreadNums = nettyServerConfig.getEventLoopGroupBossThreadNums();
        final int eventLoopGroupSelectorThreadNums = nettyServerConfig.getEventLoopGroupSelectorThreadNums();

        if (useEpoll()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(eventLoopGroupBossThreadNums, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("Thread-EventLoopGroupBoss-%d-%d", eventLoopGroupBossThreadNums, threadIndex.getAndIncrement()));
                }
            });

            this.eventLoopGroupSelector = new EpollEventLoopGroup(eventLoopGroupSelectorThreadNums, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("Thread-EventLoopGroupSelector-%d-%d", eventLoopGroupSelectorThreadNums, threadIndex.getAndIncrement()));
                }
            });
        } else {

            this.eventLoopGroupBoss = new NioEventLoopGroup(eventLoopGroupBossThreadNums, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("Thread-EventLoopGroupBoss-%d-%d", eventLoopGroupBossThreadNums, threadIndex.getAndIncrement()));
                }
            });

            this.eventLoopGroupSelector = new NioEventLoopGroup(eventLoopGroupSelectorThreadNums, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("Thread-EventLoopGroupSelector-%d-%d", eventLoopGroupSelectorThreadNums, threadIndex.getAndIncrement()));
                }
            });
        }
    }

    /**
     * 判断是否使用Epoll来进行通信
     */
    private boolean useEpoll() {

        return false;
    }

    @Override
    public void start() {
        // 【创建事件处理线程池】
        // 【创建共用的Handler】
        // 【设置处理线程池】
        // 【设置参数】
        // 【设置监听端口】
        // 【设置Handler】
        // 【开启服务端】

        final int defaulEventExecutorThreadNums = this.nettyServerConfig.getDefaultEventExecutorThreadNums();
        this.defaultEventExecutor = new DefaultEventExecutorGroup(defaulEventExecutorThreadNums, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("Thread-defaultEventExecutor-%d-%d", defaulEventExecutorThreadNums, threadIndex.getAndIncrement()));
            }
        });

        // 创建共用的 Handler
        //    编码NettyEncoder  处理服务端业务serverHandler
        prepareSharableHandlers();

        //
        ServerBootstrap childHandler = this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                .childOption(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(defaultEventExecutor,
                                        /*编码*/
                                        nettyEncoder,
                                        /*解码*/
                                        new NettyDecoder(),
                                        /*心跳*/
                                        new IdleStateHandler(0, 0, 120),
                                        /*业务Handler*/
                                        nettyServerHandler);

                    }
                });

        // todo 共享内存池
        childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        try {
            // 启动服务端，进行监听
            this.serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("serverBootstrap.bind().sync() InterruptedException", e);
        }
    }

    private void prepareSharableHandlers() {
        this.nettyEncoder = new NettyEncoder();
        this.nettyServerHandler = new NettyServerHandler();
    }

    @Override
    public void shutDown() {
        // 【关闭各个线程池】
        this.eventLoopGroupSelector.shutdownGracefully();
        this.eventLoopGroupBoss.shutdownGracefully();
        this.defaultEventExecutor.shutdownGracefully();
    }

    @Override
    public void registerProcesser(int requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        if (executor == null) {
            executor = this.publicExecutor;
        }
        Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<>(processor, executor);
        this.processorTable.put(requestCode, pair);
    }

    class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) throws Exception {
            processMessageReceived(channelHandlerContext, remotingCommand);
        }
    }
}
