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
public class NettyServerConfig {
    private int listenPort = 8888;
    private int publicThreadNums = 10;
    private int eventLoopGroupSelectorThreadNums = 10;
    private int eventLoopGroupBossThreadNums = 10;
    private int defaultEventExecutorThreadNums = 10;

}
