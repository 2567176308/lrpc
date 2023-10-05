package org.lrpc.core.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用来描述，请求调用方所有请求的方法接口的描述
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPayload implements Serializable {

//    1、接口的名字
    private String interfaceName; // org.rpc.demo.HelloLrpc

//    2、方法的名字
    private String methodName;

//    3、参数列表，分为参数类型和具体参数
//    参数类型用来确定重载方法、具体的参数用来执行方法调用
    private Class<?>[] parametersType;
    private Object[] parametersValues;

//    4、返回值的封装
    private Class<?> returnType;
}
