package org.lrpc.core.compress;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.compress.impl.GzipCompressor;
import org.lrpc.core.serializer.SerializerWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解压器的工厂
 */
@Slf4j
public class CompressorFactory {
    private static final Map<String,CompressorWrapper > COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<Byte,CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        CompressorWrapper gzip = new CompressorWrapper((byte) 1,"gzip",new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip",gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1,gzip);
    }

    public static  CompressorWrapper getCompressor(String compressorType) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorWrapper == null) {
            log.error("未找到您配置的[{}]的压缩策略，默认选用gzip压缩方式",compressorType);
            return COMPRESSOR_CACHE.get("gzip");
        }

        return compressorWrapper;
    }
    public static CompressorWrapper getCompressor(byte compressorTypeCode) {
        return COMPRESSOR_CACHE_CODE.get(compressorTypeCode);
    }



}