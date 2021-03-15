package org.example.nettylearning.demo7;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


public class SubReqClient {
    public static void main(String[] args) {
        int port = 8880;
        if(args != null && args.length > 0){
            try {
                port = Integer.parseInt(args[0]);
            }catch (NumberFormatException exception){
                exception.printStackTrace();
                //采用默认值
            }
        }

        new SubReqClient().connect(port,"127.0.0.1");
    }

    private void connect(int port, String host) {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootStrap = new Bootstrap();
            bootStrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())))
                                    .addLast(new ObjectEncoder())
                                    .addLast(new SubReqClientHandler());
                        }
                    });
            //发起异步连接操作
            ChannelFuture future = bootStrap.connect(host,port).sync();
            //等待客户端链路关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
