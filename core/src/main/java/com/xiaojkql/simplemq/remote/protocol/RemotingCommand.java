package com.xiaojkql.simplemq.remote.protocol;

import com.xiaojkql.simplemq.remote.CommandCustomHeader;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description 传输协议
 */
@Data
public class RemotingCommand {

    // 消息体
    private transient byte[] body;
    // 定制的消息头
    private transient CommandCustomHeader commandCustomHeader;

    // 序列化的方式
    private SerializeType serializeType = SerializeType.SIMPLEMQ;

    private HashMap<String, String> extFields;
    // 请求码、响应码
    private int code;
    // 通信标识
    private int opaque;

    public static RemotingCommand decode(ByteBuffer byteBuffer) {
        // 总长度
        int totalLength = byteBuffer.limit();
        // 获取消息头部长度
        int headerLength = byteBuffer.getInt() & 0xFFFFFF /*3个字节*/;
        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);
        byte serializeCode = (byte) ((byteBuffer.getInt() >> 24) & 0xFF);
        SerializeType serializeType = SerializeType.valueOf(serializeCode);

        decodeHeader(headerData, serializeType);

        return null;
    }

    private static RemotingCommand decodeHeader(byte[] headerData, SerializeType serializeType) {
        return SimpleMQSerializer.decode(headerData);

    }

    public ByteBuffer encodeHeader() {
        return encodeHeader(body == null ? 0 : body.length);
    }

    /**
     * |  4 byte  |                 4 byte           |  header-date  |  body-data |
     * | 消息总长度 | 消息头部信息（头部序列化方式+头部长度） |    消息头部     |   消息体    |
     */
    public ByteBuffer encodeHeader(int bodyLength) {
        // 用于放置消息总长度 byte[4]
        int totalLength = 4;
        // 用于放置消息头部信息 byte[4]
        totalLength += 4;
        // 将头部转换为字节数组
        byte[] headerData = this.headerEncode();
        // 消息头长度
        totalLength += headerData.length;
        // 消息体长度
        totalLength += bodyLength;
        // 分配内存
        // 这里仅仅存入消息头，不放入消息体，
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength - bodyLength);
        // 消息总长度
        byteBuffer.putInt(totalLength);
        // 消息头信息
        byteBuffer.put(addProtocolTypeInfo(headerData.length));
        // 消息头
        byteBuffer.put(headerData);
        return byteBuffer;
    }

    private byte[] headerEncode() {
        this.putHeaderToExtFields();
        return SimpleMQSerializer.encode(this);
    }

    private void putHeaderToExtFields() {
        if (this.commandCustomHeader != null) {
            Field[] declaredFields = this.commandCustomHeader.getClass().getDeclaredFields();
            if (this.extFields == null) {
                this.extFields = new HashMap<>();
            }

            for (Field field : declaredFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    if (!field.getName().startsWith("this")) {

                        Object value = null;
                        try {
                            field.setAccessible(true);
                            value = field.get(this.commandCustomHeader);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        if (value != null) {
                            this.extFields.put(field.getName(), value.toString());
                        }
                    }
                }
            }

        }
    }

    private byte[] addProtocolTypeInfo(int headerLength) {
        byte[] res = new byte[4];
        res[0] = serializeType.getCode();
        // 存入消息头长度 前3个字节
        res[1] = (byte) ((headerLength >> 16) & 0xFF);
        res[1] = (byte) ((headerLength >> 8) & 0xFF);
        res[1] = (byte) (headerLength & 0xFF);
        return res;
    }


}
