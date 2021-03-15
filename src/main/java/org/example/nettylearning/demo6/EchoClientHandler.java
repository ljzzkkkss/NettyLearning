package org.example.nettylearning.demo6;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class EchoClientHandler extends ChannelHandlerAdapter {
    private int counter;
    private static final String ECHO_REQ = "Liu Jun Welcome to Netty at Nanjing";

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        for(int i = 0; i < 10; i++) {
            ByteBuf req = Unpooled.copiedBuffer(ECHO_REQ.getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(req);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
