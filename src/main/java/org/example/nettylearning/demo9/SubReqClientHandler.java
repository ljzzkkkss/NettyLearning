package org.example.nettylearning.demo9;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class SubReqClientHandler extends ChannelHandlerAdapter {
    private int counter;
    private static final String ECHO_REQ = "Liu Jun Welcome to Netty at Nanjing";

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        for(int i = 0; i < 10; i++) {
            ctx.writeAndFlush(subReq(i));
        }
    }

    private Object subReq(int i) {
        SubscribeReq req = new SubscribeReq();
        req.setSubReqID(i);
        req.setUserName("Liu Jun");
        req.setPhoneNumber("13800000000");
        req.setProductName("Netty for marshalling");
        req.setAddress("江苏省南京市江宁区上元大街");
        return req;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Receive server response : [" + msg + "]");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
