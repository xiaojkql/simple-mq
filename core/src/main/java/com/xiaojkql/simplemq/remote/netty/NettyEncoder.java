package com.xiaojkql.simplemq.remote.netty;

import com.xiaojkql.simplemq.remote.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand, ByteBuf out) throws Exception {
        ByteBuffer byteBuffer = remotingCommand.encodeHeader();
        out.writeBytes(byteBuffer);
        byte[] body = remotingCommand.getBody();
        if (body != null) {
            out.writeBytes(body);
        }
    }
}
