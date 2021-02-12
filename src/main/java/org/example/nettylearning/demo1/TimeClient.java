package org.example.nettylearning.demo1;

import java.io.IOException;

public class TimeClient {

    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        int port = 8880;
        if(args != null && args.length > 0){
            try {
                port = Integer.parseInt(args[0]);
            }catch (NumberFormatException exception){
                exception.printStackTrace();
                //采用默认值
            }
        }

        new Thread(new TimeClientHandle("127.0.0.1",port),"TimeClient-001").start();
    }
}
