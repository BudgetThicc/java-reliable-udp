package team.nameless.stp;

import java.io.*;
import java.net.*;

public class Sender{

    static int PORT=8888;
    static String IP="localhost";
    public static void main(String[] args){
        String rcvIP=args[0];
        int rcvPort=Integer.parseInt(args[1]);
        String filename=args[2];
        int MSS=Integer.parseInt(args[3]);
        int windowSize=Integer.parseInt((args[4]));

        SenderThread thread=new SenderThread(windowSize,MSS,PORT,IP);
        thread.setDes(rcvIP,rcvPort);

        try {
            thread.setData(readFile(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t=new Thread(thread);
        t.start();

    }

    private static byte[] readFile(String filename) throws IOException {
        BufferedInputStream bis = null;
        bis = new BufferedInputStream(new FileInputStream(filename));
        byte[] temp=new byte[10000];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int size = 0;
        while( (size = bis.read(temp)) !=-1){
            baos.write(temp,0,size);
        }
        return baos.toByteArray();
    }


}