package org.example.nettylearning.normalsocketserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            socket = new Socket("127.0.0.1", port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
            while(true) {
                long start = System.currentTimeMillis();
                out.println("QUERY TIME ORDER");
                System.out.println("Send order 2 server succeed.");
                String resp = in.readLine();
                System.out.println("Now is : " + resp);
                long used = System.currentTimeMillis() - start;
                Thread.sleep(1000 - used);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != out){
                out.close();
            }
            if(null != in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != socket){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
