package com.xiaojkql.simplemq.remote.netty;

import com.xiaojkql.simplemq.remote.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteBuffer;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description 继承
 */
public class NettyDecoder extends LengthFieldBasedFrameDecoder {
    public NettyDecoder() {
        // todo 具体设置
        // lengthFieldLength：长度占的字节数
        super(16777216, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame != null) {
            ByteBuffer byteBuffer = frame.nioBuffer();
            return RemotingCommand.decode(byteBuffer);
        }
        return null;
    }
}
