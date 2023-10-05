package org.lrpc.core.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LrpcResponse {

    //    请求ID
    private long requestId;
    //    请求类型、压缩类型、序列化方式
    private byte requestType;

    private byte compressType;
    private byte serializeType;

//    响应码
    private  byte code;

//    具体的消息体
    private Object body;
}
