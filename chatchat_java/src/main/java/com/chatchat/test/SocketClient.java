package com.chatchat.test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Scanner;

public class SocketClient {
    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket=new Socket("localhost",1024);
            OutputStream outputStream=socket.getOutputStream();
            PrintWriter printWriter=new PrintWriter(outputStream);
            System.out.println("请输入内容：");
            new Thread(() -> {
                while (true) {
                    Scanner scanner=new Scanner(System.in);
                    String input=scanner.nextLine();
                    try {
                        printWriter.println(input);
                        printWriter.flush();
                    }catch (Exception e){
                        e.printStackTrace();
                        break;
                    }
                }
            }).start();

            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            new Thread(() -> {
                while (true) {
                    try {
                        String line = bufferedReader.readLine();
                        System.out.println("收到服务器消息：" + line);
                    }  catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
