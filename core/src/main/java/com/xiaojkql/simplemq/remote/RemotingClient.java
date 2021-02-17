package com.xiaojkql.simplemq.remote;

import com.xiaojkql.simplemq.remote.netty.NettyRequestProcessor;

import java.util.concurrent.ExecutorService;

/**
 * @author qinyuan xiaojkql@163.com
 * date 2021/2/17
 */
public interface RemotingClient extends RemotingService {
    void registerProcesser(int requestCode, NettyRequestProcessor processor, ExecutorService executor);
}
