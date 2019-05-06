package team.nameless.stp;

import java.net.*;

public class Sender{

    static int PORT=12500;
    public static void main(String[] args) throws SocketException {
        try{
            DatagramSocket socket = new DatagramSocket(PORT);

            String msg = "沙发凯撒货到付款举案说法凯撒好看";
            byte[] buffer = msg.getBytes();
            int msgSize = buffer.length;
            InetAddress toAdd = InetAddress.getLocalHost();
            int toPort = 11500;

            DatagramPacket pack = new DatagramPacket(buffer, msgSize, toAdd, toPort);
            socket.send(pack);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}