package org.lrpc.core.serializer;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.serializer.impl.HessianSerializer;
import org.lrpc.core.serializer.impl.JdkSerializer;
import org.lrpc.core.serializer.impl.JsonSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class SerializerFactory {

    private static final Map<String,SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private static final Map<Byte,SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();


    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());

        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);

        SERIALIZER_CACHE_CODE.put((byte) 1,jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2,json);
        SERIALIZER_CACHE_CODE.put((byte) 3,hessian);
    }

    /**
     * 使用工厂方法获取一个serializerWrapper
     * @param serializerType 序列化类型
     * @return SerializerWrapper
     */
    public static SerializerWrapper getSerializer(String serializerType) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializerType);
        if (serializerWrapper == null) {
            log.error("未找到您配置的[{}]的序列化策略，默认选用jdk序列化方式",serializerType);
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

    public static SerializerWrapper getSerializer(byte serializerTypeCode) {

        return SERIALIZER_CACHE_CODE.get(serializerTypeCode);
    }
}
