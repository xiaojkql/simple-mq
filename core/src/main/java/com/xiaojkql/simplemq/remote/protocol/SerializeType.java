package com.xiaojkql.simplemq.remote.protocol;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
public enum SerializeType {
    ROCKETMQ((byte) 0);

    private byte code;

    SerializeType(byte code) {
        this.code = code;
    }

    public static SerializeType valueOf(byte code) {
        SerializeType.values();
        for (SerializeType serializeType : SerializeType.values()) {
            if (serializeType.getCode() == code) {
                return serializeType;
            }
        }
        throw new RuntimeException("序列化方式不对");
    }

    public byte getCode() {
        return code;
    }
}
