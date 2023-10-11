package org.lrpc.core.compress.impl;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.core.compress.Compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZip具体实现
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("gzip对字节数组压缩完成,长度由[{}],转化为[{}].",bytes.length,result.length);
            }
            return result;
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("对字节数组压缩时发生异常");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {

        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(bais)) {
            byte[] result = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("gzip对字节数组解压缩完成,长度由[{}],转化为[{}].",bytes.length,result.length);
            }
            return result;
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("对字节数组解压缩时发生异常");
            }
            throw new RuntimeException(e);
        }
    }
}
