package team.nameless.stp;

import java.net.*;

public class Sender{

    static int PORT=8888;
    static String IP="localhost";
    public static void main(String[] args){
        String rcvIP=args[0];
        int rcvPort=Integer.parseInt(args[1]);
        String filename=args[2];
        int MSS=Integer.parseInt(args[3]);

        SenderThread thread=new SenderThread(MSS,PORT,IP);
        thread.setDes(rcvIP,rcvPort);
        thread.setData("打撒换个卡老师的金刚护法看电视看了哈电风扇");
        Thread t1=new Thread(thread);
        t1.start();
    }


}