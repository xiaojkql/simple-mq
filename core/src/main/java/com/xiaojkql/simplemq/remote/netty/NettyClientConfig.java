package com.xiaojkql.simplemq.remote.netty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NettyClientConfig {
    private int eventLoopGroupWorkerThreadNums = 10;
    private int publicThreadNums = 10;
    private int defaultEventExecutorThreadNums = 10;
}
