package org.lrpc.core.transport.message;

import java.nio.charset.StandardCharsets;

public class MessageFormatConstant {
    public static final byte[] MAGIC = "lrpc".getBytes(StandardCharsets.UTF_8);

    public static final byte VERSION = 1;
//    public static final short HEADER_LENGTH = 2;
    public static final short HEADER_LENGTH = (byte) (MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);

//    头部信息长度占用字节数
    public static final int HEADER_FIELD_LENGTH = 2;
    public static final int MAX_FRAME_LENGTH = 1024 * 1024;
    public static final int VERSION_LENGTH = 1;
//    总长度占用的字节数
    public static final int FULL_FILED_LENGTH = 4;
}
