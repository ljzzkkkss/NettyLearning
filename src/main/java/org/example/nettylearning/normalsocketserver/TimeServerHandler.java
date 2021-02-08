package org.example.nettylearning.normalsocketserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class TimeServerHandler implements Runnable {
    private final Socket socket;

    public TimeServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
            String currentTime;
            String body;
            while(true){
                body = in.readLine();
                if(null == body){break;}
                System.out.println("The time server receive order : " + body);
                currentTime = "QUERY TIME ORDER".equals(body) ? new Date().toString() : "BAD ORDER";
                out.println(currentTime);
            }

        } catch (IOException e) {
            if(null != in){
                try {
                    in.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if(null != out){
                out.close();
            }
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
