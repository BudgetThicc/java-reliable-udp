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
        int MWS=Integer.parseInt((args[4]));
        int timeout=Integer.parseInt(args[5]);
        double pdrop=Double.parseDouble(args[6]);
        long seed=Long.parseLong(args[7]);

        SenderThread sender=new SenderThread(MWS,MSS,PORT,IP,timeout);
        sender.setDes(rcvIP,rcvPort);
        PLD.setRandom(seed,pdrop);


        try {
            sender.setData(readFile(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t=new Thread(sender);
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