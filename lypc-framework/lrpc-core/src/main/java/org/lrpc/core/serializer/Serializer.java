package org.lrpc.core.serializer;

/**
 * 序列化器
 */
public interface Serializer {

    /**
     * 抽象用于做序列化的方法
     * @param object 待序列化实例对象
     * @return 字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化方法
     * @param bytes 待反序列化字节数组
     * @return 具体实例化对象
     * @param <T> 对象具体类型
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
