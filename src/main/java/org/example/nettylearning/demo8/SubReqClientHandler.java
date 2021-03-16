package org.example.nettylearning.demo8;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

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
        SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
        builder.addSubReqID(i);
        builder.addUserName("Liu Jun");
        builder.addProductName("Netty book");
        List<String> addressList = new ArrayList<>();
        addressList.add("NanJing YuHuaTai");
        addressList.add("BeiJing LiuLiChang");
        addressList.add("ShenZhen HongShuLin");
        builder.addAllAddress(addressList);
        return builder.build();
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
