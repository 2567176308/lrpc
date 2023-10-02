package com.lrpc;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipTest {

    private final byte[] unZipBytes = {12,32,43,23,12,53,12,43,65,12,32,43,23,12,53,12,43,65,12,32,43,23,12,53,12,43,65,3,23,12,32,43,23,12,53,12,43,65,21,12,32,43,23,12,53,12,43,65};
    private byte[] zipBytes = {31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -29, 81, -48, 22, -25, 49, -27, -47, 118, -28, -63, 96, 48, -117, -61, -103, -94, 112, 22, 0, -66, 49, 100, 48, 48, 0, 0, 0};
    @Test
    public void compress() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
        gzipOutputStream.write(unZipBytes);
        gzipOutputStream.flush();
        gzipOutputStream.close();
        zipBytes = outputStream.toByteArray();
        System.out.println("unzip ->" +unZipBytes.length);
        System.out.println("ziped ->" + zipBytes.length);
    }

    @Test
    public void decompress() throws IOException {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        byte[] bytes = gzipInputStream.readAllBytes();
        System.out.println(Arrays.toString(bytes));

    }
}
