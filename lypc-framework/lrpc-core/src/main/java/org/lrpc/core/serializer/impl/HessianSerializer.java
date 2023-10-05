package org.lrpc.core.serializer.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.common.exception.SerializeException;
import org.lrpc.core.serializer.Serializer;
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            HessianOutput output = new HessianOutput(os);
            output.writeObject(object);
            data = os.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("已经完成对[{}]的序列化",object);
            }
        } catch (Exception e) {
            throw new SerializeException("hessian序列化发生异常");
        }
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes==null){
            return null;
        }
        Object result = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            HessianInput input = new HessianInput(is);
            result = input.readObject();
            if (log.isDebugEnabled()) {
                log.debug("已经完成对[{}]的反序列化",result);
            }
        } catch (Exception e) {
            throw new SerializeException("hessian发序列化发生异常");
        }
        return (T)result;

    }
}
