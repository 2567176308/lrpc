package org.lrpc.core.serializer.impl;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.common.exception.SerializeException;
import org.lrpc.core.serializer.Serializer;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream)){

            objectOutputStream.writeObject(object);

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("已经完成对[{}]的序列化",object);
            }
            return byteArray;
        } catch (IOException e) {
            log.error("序列化出现异常");
            throw new SerializeException("序列化时发生异常");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes,Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        ) {
            T t = (T) objectInputStream.readObject();
            if (log.isDebugEnabled()) {
                log.debug("已经完成对[{}]的反序列化",t);
            }
            return t;
        }catch (Exception e) {
            log.error("请求[{}]序列化时发生了异常",clazz,e);
            throw new RuntimeException("序列化异常");
        }
    }
}
