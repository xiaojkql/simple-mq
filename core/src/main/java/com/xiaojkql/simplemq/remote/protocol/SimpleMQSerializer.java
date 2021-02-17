package com.xiaojkql.simplemq.remote.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author qinyuan xiaojkql@163.com
 * @date 2021/2/17
 * @description
 */
public class SimpleMQSerializer {

    private static Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    /**
     * | code 2 byte | opaque 4 byte | extLength 4 byte | extFields |
     */
    public static byte[] encode(RemotingCommand cmd) {

        byte[] extFields = null;
        int extLength = 0;
        if (cmd.getExtFields() != null && !cmd.getExtFields().isEmpty()) {
            extFields = serializeMapToByte(cmd.getExtFields());
            extLength = extFields.length;
        }

        int totalLength = 2 + /*short code */
                4 + /*int opaque*/
                4 + /*int extLength*/
                extLength /*extFields*/;

        ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength);
        byteBuffer.putShort((short) cmd.getCode());
        byteBuffer.putInt(cmd.getOpaque());
        byteBuffer.putInt(extLength);
        if (extFields != null && extFields.length != 0) {
            byteBuffer.put(extFields);
        }
        return byteBuffer.array();
    }

    /**
     * | keySize (short 2 byte)| keyValue  |  valueSize（int 4 byte） | value |
     */
    private static byte[] serializeMapToByte(HashMap<String, String> map) {

        // 两步
        // 1 【计算序列化后的总长度】
        // 2 【序列化map 生成ByteBuffer】
        // TODO 各种格式校验 null 校验，空校验

        Iterator<Map.Entry<String, String>> entryIterator = map.entrySet().iterator();

        int totalLength = 0;
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            totalLength += 2 + entry.getKey().getBytes(CHARSET_UTF8).length
                    + 4 + entry.getValue().getBytes(CHARSET_UTF8).length;
        }

        ByteBuffer res = ByteBuffer.allocate(totalLength);

        entryIterator = map.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();

            res.putShort((short) 2);
            res.put(entry.getKey().getBytes(CHARSET_UTF8));

            res.putInt(4);
            res.put(entry.getValue().getBytes(CHARSET_UTF8));

        }


        return res.array();
    }

    /**
     * | code 2 byte | opaque 4 byte | extLength 4 byte | extFields |
     */
    public static RemotingCommand decode(byte[] headerData) {
        RemotingCommand cmd = new RemotingCommand();
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerData);

        short code = byteBuffer.getShort();
        cmd.setCode(code);
        int opaque = byteBuffer.getInt();
        cmd.setOpaque(opaque);
        int extLength = byteBuffer.getInt();
        byte[] extFields = new byte[extLength];
        byteBuffer.get(extFields);
        HashMap<String, String> extMap = deserializeMap(extFields);
        cmd.setExtFields(extMap);
        return cmd;
    }

    private static HashMap<String, String> deserializeMap(byte[] extFields) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(extFields);

        HashMap<String, String> extMap = new HashMap<>();

        byte[] keyArray;
        byte[] valueArray;

        while (byteBuffer.hasRemaining()) {
            short keyLength = byteBuffer.getShort();
            keyArray = new byte[keyLength];
            byteBuffer.get(keyArray);

            int valueLength = byteBuffer.getInt();
            valueArray = new byte[valueLength];
            byteBuffer.get(valueArray);

            extMap.put(keyArray.toString(), valueArray.toString());
        }

        return extMap;
    }
}
