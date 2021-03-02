package org.example.nettylearning.demo2;

import org.example.nettylearning.demo1.TimeClientHandle;

public class TimeClient {
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

        new Thread(new AsyncTimeClientHandle("127.0.0.1",port),"AIO-AsyncTimeClient-001").start();
    }
}
