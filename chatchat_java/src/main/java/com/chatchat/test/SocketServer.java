package com.chatchat.test;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SocketServer {
    public static void main(String[] args) {
        ServerSocket server = null;
        Map<String,Socket> ClientMap = new HashMap<>();
        try {
            server=new ServerSocket(1024);
            System.out.println("Server started");
            while (true) {
                Socket socket=server.accept();
                String ip=socket.getInetAddress().getHostAddress();
                String clientKey=ip+":"+socket.getPort();
                ClientMap.put(clientKey,socket);

                System.out.println("有客户端链接,ip："+ip+",端口："+socket.getPort());

                new Thread(() -> {
                    while (true) {
                        try {
                            InputStream inputStream = socket.getInputStream();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String line = bufferedReader.readLine();
                            System.out.println("收到客户端消息：" + line);
                            String msgIPKey=socket.getInetAddress().getHostAddress()+":"+socket.getPort();
                            ClientMap.forEach((k,v)->{
                                try {
                                    OutputStream outputStream=v.getOutputStream();
                                    PrintWriter printWriter=new PrintWriter(outputStream);
                                    printWriter.println(msgIPKey+"->"+line);
                                    printWriter.flush();
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                            break;
                        }
                    }
                }).start();
                new Thread(() -> {
                    while (true) {
                        Scanner scanner=new Scanner(System.in);
                        String input=scanner.nextLine();
                    }
                }).start();
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
