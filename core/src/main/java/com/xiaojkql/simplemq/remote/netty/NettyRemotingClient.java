package com.xiaojkql.simplemq.remote.netty;

import com.xiaojkql.simplemq.remote.InvokeCallback;
import com.xiaojkql.simplemq.remote.RemotingClient;
import com.xiaojkql.simplemq.remote.common.Pair;
import com.xiaojkql.simplemq.remote.common.RemotingHelper;
import com.xiaojkql.simplemq.remote.protocol.RemotingCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
public class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {

    private final ExecutorService publicExecutor;

    private final NettyClientConfig nettyClientConfig;
    private final Bootstrap bootstrap;
    // 轮询处理线程池
    private final EventLoopGroup eventLoopGroupWorker;

    // 编码Handler
    private NettyEncoder nettyEncoder;
    // 服务端业务handler
    private NettyClientHandler nettyClientHandler;
    // Netty SocketChannel各个事件处理线程池
    private DefaultEventExecutorGroup defaultEventExecutor;

    private ConcurrentHashMap<String/*addr*/, ChannelFuture> addr2ChannelFutureTable;

    public NettyRemotingClient(NettyClientConfig nettyClientConfig) {

        this.nettyClientConfig = nettyClientConfig;
        this.bootstrap = new Bootstrap();

        this.eventLoopGroupWorker = new NioEventLoopGroup(this.nettyClientConfig.getEventLoopGroupWorkerThreadNums(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("Thread-eventLoopGroupWorker-%d", threadIndex.getAndIncrement()));
            }
        });

        this.publicExecutor = Executors.newFixedThreadPool(this.nettyClientConfig.getPublicThreadNums(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("Thread-publicExecutor-%d", threadIndex.getAndIncrement()));
            }
        });


    }

    @Override
    public void start() {

        this.defaultEventExecutor = new DefaultEventExecutorGroup(this.nettyClientConfig.getDefaultEventExecutorThreadNums(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("Thread-defaultEventExecutor-%d", threadIndex.getAndIncrement()));
            }
        });

        this.bootstrap.group(this.eventLoopGroupWorker)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defaultEventExecutor,
                                nettyEncoder,
                                new NettyDecoder(),
                                new IdleStateHandler(0, 0, 120),
                                nettyClientHandler);
                    }
                });
    }

    @Override
    public void shutDown() {

    }


    public RemotingCommand invokeSync(RemotingCommand request, String addr) throws InterruptedException {
        Channel channel = getAndCreateChannel(addr);
        RemotingCommand remotingCommand = this.invokeSyncImpl(request, channel);
        return remotingCommand;
    }

    public RemotingCommand invokeAsync(RemotingCommand request, String addr, InvokeCallback invokeCallback/*业务回调逻辑*/) {
        Channel channel = getAndCreateChannel(addr);
        this.invokerAsyncImpl(request, channel, invokeCallback);
        return null;
    }


    private Channel getAndCreateChannel(final String addr) {
        // todo 更加细致的维护Channel的失效
        return this.addr2ChannelFutureTable.computeIfAbsent(addr, this::createChannel).channel();
    }

    private ChannelFuture createChannel(final String addr) {
        return this.bootstrap.connect(RemotingHelper.string2SocketAddress(addr));
    }

    @Override
    public void registerProcesser(int requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        if (executor == null) {
            executor = this.publicExecutor;
        }
        Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<>(processor, executor);
        this.processorTable.put(requestCode, pair);
    }

    class NettyClientHandler extends SimpleChannelInboundHandler<RemotingCommand> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
            NettyRemotingClient.this.processMessageReceived(ctx, msg);
        }
    }

}
