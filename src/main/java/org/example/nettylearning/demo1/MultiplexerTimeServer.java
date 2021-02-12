package org.example.nettylearning.demo1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable{
    private Selector selector;
    private volatile boolean stop;

    public MultiplexerTimeServer(int port){
        try{
            selector = Selector.open();//创建多路复用器
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);//设置为非阻塞模式
            serverChannel.socket().bind(new InetSocketAddress(port), 1024);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);//注册selector,监听accept操作
            System.out.println("The time server is running in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop(){
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop){
            try{
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key;
                while (iterator.hasNext()){
                    key = iterator.next();
                    iterator.remove();
                    try{
                        handleInput(key);
                    } catch (Exception e){
                        if(null != key){
                            key.cancel();
                            if(null != key.channel()){
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //selector关闭之后，注册在selector上的channel和pipe等资源会自动注册关闭，所以无需重复关闭
        if(null != selector){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException{
        if(key.isValid()){
            //处理请求
            if(key.isAcceptable()){
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = channel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector,SelectionKey.OP_READ);//注册selector，监听read操作
            }
            if(key.isReadable()){
                //读取请求
                SocketChannel socketChannel = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = socketChannel.read(readBuffer);//查看读取的字节数为-1的时候说明链路已经关闭
                if(readBytes > 0){
                    readBuffer.flip();//将当前缓冲区的limit设置为position,position设置为0
                    byte[] bytes = new byte[readBuffer.remaining()];//readBuffer.remaining()是获取缓冲区可读字节数
                    readBuffer.get(bytes);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println("The time server receive order : " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date().toString() : "BAD ORDER";
                    doWrite(socketChannel,currentTime);
                }else if(readBytes < 0){
                    //对端链路关闭
                    key.cancel();
                    socketChannel.close();
                } //读到0字节的情况忽略
            }
        }
    }

    private void doWrite(SocketChannel socketChannel, String response) throws IOException {
        if(null != response && response.trim().length() > 0){
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            socketChannel.write(writeBuffer);//由于SocketChannel是异步非阻塞的，所以不能保证一次性把需要发送的字节组发送完，存在写“半包”的问题
            //写“半包”的问题可以通过注册写操作，不断轮询Selector并使用ByteBuffer的hasRemaining方法判断是否写完去处理
        }
    }
}
