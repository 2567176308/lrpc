package com.lrpc;

import com.lrpc.netty.AppClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NettyTest {

    @Test
    public void testCompositeByteBuf() {
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();

//        通过逻辑组装而不是物理拷贝，实现在jvm中的零拷贝
        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.addComponents(header,body);

    }

    @Test
    public void testWrapper() {
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];
//        共享byte数组的内容而不是拷贝。
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf,buf2);
    }

    @Test
    public void testSlice() {
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];

//        共享byte数组的内容而不是拷贝。
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf,buf2);
        ByteBuf buf1 = byteBuf.slice(1,5);
        ByteBuf buf3 = byteBuf.slice(6,15);
    }
    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        message.writeBytes("lrpc".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(212321L);

        AppClient appClient = new AppClient();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(appClient);
        byte[] buffer = outputStream.toByteArray();
        message.writeBytes(buffer);

        System.out.println(message);

        printAsBinary(message);
    }
    public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(),bytes);
        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i+=2) {
            formattedBinary.append(binaryString.substring(i,i + 2)).append(" ");
        }
        System.out.println("Binary representation:" + formattedBinary.toString());
    }
}
