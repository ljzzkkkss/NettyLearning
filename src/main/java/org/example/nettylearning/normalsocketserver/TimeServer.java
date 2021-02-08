package org.example.nettylearning.normalsocketserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeServer {
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
        ServerSocket server = null;
        try{
            server = new ServerSocket(port);
            System.out.println("The time server is running on port : " + port);
            Socket socket = null;
            TimeServerHandlerExecutePool pool = new TimeServerHandlerExecutePool(50,10000);
            while (true){
                socket = server.accept();
                pool.execute(new TimeServerHandler(socket));
            }
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != server){
                System.out.println("The time server close");
                server.close();
            }
        }
    }
}
