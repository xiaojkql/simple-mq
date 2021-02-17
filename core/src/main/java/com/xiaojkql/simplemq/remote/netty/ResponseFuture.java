package com.xiaojkql.simplemq.remote.netty;

import com.xiaojkql.simplemq.remote.InvokeCallback;
import com.xiaojkql.simplemq.remote.protocol.RemotingCommand;
import io.netty.channel.Channel;
import lombok.Data;

import java.util.concurrent.CountDownLatch;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
@Data
public class ResponseFuture {
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private RemotingCommand responseCommand;
    private int opaque;
    private Channel channel;
    private InvokeCallback invokeCallback;

    private volatile Throwable cause;

    public ResponseFuture(int opaque, Channel channel, InvokeCallback invokeCallback) {
        this.opaque = opaque;
        this.channel = channel;
    }

    public RemotingCommand awaitResponse() throws InterruptedException {
        // 等待阻塞
        this.countDownLatch.await();
        return this.responseCommand;
    }

    public void putResponse(RemotingCommand responseCommand) {
        // 唤醒等待
        this.responseCommand = responseCommand;
        this.countDownLatch.countDown();
    }

    public void executeCallback() {
        if (this.invokeCallback != null) {
            this.invokeCallback.operationComplete(this);
        }
    }

}
