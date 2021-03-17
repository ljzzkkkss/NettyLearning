package org.example.nettylearning.httpdemo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {
    private static final String DEFAULT_URL = "/src/file";

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

        String url = DEFAULT_URL;
        if(args != null && args.length > 1){
            url = args[1];
        }
        new HttpFileServer().run(port,url);
    }

    public void run(final int port,final String url){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast("http-decoder",new HttpRequestDecoder())
                                    .addLast("http-aggregator",new HttpObjectAggregator(65535))
                                    .addLast("http-encoder",new HttpResponseEncoder())
                                    .addLast("http-chunked",new ChunkedWriteHandler())
                                    .addLast("fileServerHanderl", new HttpFileServerHandler(url));
                        }
                    });
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind("172.19.37.110",port).sync();
            System.out.println("HTTP file server start on : http://172.19.37.110:" + port + url);
            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
