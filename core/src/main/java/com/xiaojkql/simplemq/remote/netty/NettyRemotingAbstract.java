package com.xiaojkql.simplemq.remote.netty;

import com.xiaojkql.simplemq.remote.InvokeCallback;
import com.xiaojkql.simplemq.remote.common.Pair;
import com.xiaojkql.simplemq.remote.protocol.RemotingCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description 【灵活使用final关键字】
 */
public abstract class NettyRemotingAbstract {

    protected final HashMap<Integer/*协议码code*/, Pair<NettyRequestProcessor, ExecutorService>> processorTable =
            new HashMap<>();
    protected final ConcurrentHashMap<Integer/*opaque*/, ResponseFuture> responseTable = new ConcurrentHashMap<>();

    // 处理消息的逻辑
    public void processMessageReceived(ChannelHandlerContext ctx, RemotingCommand msg) {
        final RemotingCommand cmd = msg; // 应该定义为final类型
        if (cmd != null) {
            switch (cmd.getType()) {
                case REQUEST_COMMAND:
                    processRequestCommand(ctx, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponseCommand(ctx, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    public RemotingCommand invokeSyncImpl(RemotingCommand request, Channel channel) throws InterruptedException {
        // 请求标识
        final int opaque = 0;
        final ResponseFuture responseFuture = new ResponseFuture(opaque, channel, null);
        this.responseTable.put(opaque, responseFuture);
        // 调用Channel向外发送消息
        try {
            channel.writeAndFlush(request)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                return;
                            }
                            responseTable.remove(opaque);
                            responseFuture.putResponse(null);
                            responseFuture.setCause(future.cause());
                        }
                    });
            // 同步等待消息
            return responseFuture.awaitResponse();
        } finally {
            responseTable.remove(opaque);
        }
    }

    public RemotingCommand invokerAsyncImpl(RemotingCommand request, Channel channel, InvokeCallback invokeCallback/*业务相关的回调函数*/) {
        final int opaque = 1;
        final ResponseFuture responseFuture = new ResponseFuture(opaque, channel, invokeCallback);
        this.responseTable.put(opaque, responseFuture);
        channel.writeAndFlush(request)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            return;
                        }
                        requestFail(responseFuture);
                    }
                });

        return null;
    }

    private void requestFail(ResponseFuture responseFuture) {
        responseFuture.putResponse(null);
        // 处理 todo 单独的线程池来进行处理回调逻辑
        responseFuture.executeCallback();
    }

    public void processRequestCommand(final ChannelHandlerContext ctx, final RemotingCommand cmd) {

    }

    public void processResponseCommand(ChannelHandlerContext ctx, RemotingCommand cmd) {

    }


}
