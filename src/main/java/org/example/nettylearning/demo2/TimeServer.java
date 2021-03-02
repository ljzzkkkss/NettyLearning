package org.example.nettylearning.demo2;

public class TimeServer {
    public static void main(String[] args) {
        int port = 8080;
        if(null != args && args.length > 0){
            try{
                port = Integer.parseInt(args[0]);
            }catch (NumberFormatException exception){
                exception.printStackTrace();
                //采用默认值
            }
        }

        AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
        new Thread(timeServer,"AIO-MultiplexerTimeServer-001").start();
    }
}
