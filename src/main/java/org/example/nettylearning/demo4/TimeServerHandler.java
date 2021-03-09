package org.example.nettylearning.demo4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TimeServerHandler extends ChannelHandlerAdapter {
    private static final InternalLogger logger = Slf4JLoggerFactory.getInstance(TimeClient.class);
    private int count;

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        String body = (String) msg;
        logger.info("The time server receive order : " + body + ";The counter is : " + ++count);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date().toString() : "BAD ORDER";
        ByteBuf resp = Unpooled.copiedBuffer((currentTime + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        ctx.write(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Unexpected exception : " + cause.getMessage());
        ctx.close();
    }
}
