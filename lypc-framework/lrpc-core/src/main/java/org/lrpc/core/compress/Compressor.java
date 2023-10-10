package org.lrpc.core.compress;

/**
 * 压缩器、压缩策略
 */
public interface Compressor {

    /**
     * 压缩
     * @param bytes 待压缩字节数组
     * @return 压缩后
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压
     * @param bytes 待解压字节数组
     * @return 解压后
     */
    byte[] decompress(byte[] bytes);
}
