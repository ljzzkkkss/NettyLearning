package org.example.nettylearning.demo3;

import com.sun.istack.internal.logging.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.nio.charset.StandardCharsets;

public class TimeClientHandler extends ChannelHandlerAdapter {
    private static final InternalLogger logger = Slf4JLoggerFactory.getInstance(TimeClient.class);
    private final ByteBuf firstMessage;

    public TimeClientHandler() {
        byte[] req = "QUERY TIME ORDER".getBytes(StandardCharsets.UTF_8);
        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req,StandardCharsets.UTF_8);
        logger.info("Now is : " + body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //释放资源
        logger.warn("Unexpected exception from downstream : " + cause.getMessage());
        ctx.close();
    }
}
