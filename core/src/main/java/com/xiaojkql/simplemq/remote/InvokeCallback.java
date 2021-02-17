package com.xiaojkql.simplemq.remote;

import com.xiaojkql.simplemq.remote.netty.ResponseFuture;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
public interface InvokeCallback {
    void operationComplete(ResponseFuture responseFuture);
}
