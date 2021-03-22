package org.example.nettylearning.websocketdemo;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private WebSocketServerHandshaker handShaker;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        //传统http接入
        if (msg instanceof FullHttpRequest){
            handleHttpRequest(ctx,(FullHttpRequest) msg);
        }
        //websocket 接入
        else if(msg instanceof WebSocketFrame){
            handleWebsocketFrame(ctx,(WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req){
        //如果http解码失败，返回Http异常
        if(!req.getDecoderResult().isSuccess() || (!"websocket".equalsIgnoreCase(req.headers().get("Upgrade")))){
            sendHttpResponse(ctx,req,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        //构造握手响应返回，本机测试
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:" + WebsocketServer.port + "/websocket",null,false);
        handShaker = wsFactory.newHandshaker(req);
        if(null == handShaker){
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        }else {
            handShaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebsocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){
        //判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame){
            handShaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }

        //判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame){
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //本例程只支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)){
            throw new UnsupportedOperationException(String.format("%s frame types not supported",frame.getClass().getName()));
        }

        //返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        logger.warn(String.format("%s received %s",ctx.channel(),request));
        ctx.channel().write(new TextWebSocketFrame(request + " Welcome to use netty websocket server,now is : " + new Date().toString()));
    }

    public void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res){
        //返回应答给客户端
        if (!HttpResponseStatus.OK.equals(res.getStatus())){
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), StandardCharsets.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res,res.content().readableBytes());
        }

        //如果非Keep-Alive，关闭连接
        ChannelFuture future = ctx.channel().writeAndFlush(res);
        if(!HttpHeaders.isKeepAlive(req) || !HttpResponseStatus.OK.equals(res.getStatus())){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
