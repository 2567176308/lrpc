package org.lrpc.core.compress.impl;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.compress.Compressor;
import org.xerial.snappy.Snappy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZip具体实现
 */
@Slf4j
public class SnappyCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try {
            byte[] result = Snappy.compress(bytes);
            if (log.isDebugEnabled()) {
                log.debug("snappy对字节数组压缩完成,长度由[{}],转化为[{}].",bytes.length,result.length);
            }
            return result;
        } catch (IOException e) {

            if (log.isDebugEnabled()) {
                log.debug("snappy 对字节数组压缩时发生异常");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try {
            byte[] result = Snappy.uncompress(bytes);
            if (log.isDebugEnabled()) {
                log.debug("对字节数组解压完成,长度由[{}],转化为[{}].",bytes.length,result.length);
            }
            return result;
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("对字节数组解压时发生异常");
            }
            throw new RuntimeException(e);
        }
    }
}
