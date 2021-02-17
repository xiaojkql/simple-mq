package com.xiaojkql.simplemq.remote.common;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
public class RemotingHelper {
    public static SocketAddress string2SocketAddress(final String addr) {
        int split = addr.indexOf(':');
        String host = addr.substring(0, split);
        String port = addr.substring(split + 1);
        return new InetSocketAddress(host, Integer.valueOf(port));
    }

}
