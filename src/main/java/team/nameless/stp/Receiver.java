package team.nameless.stp;

import java.net.*;

public class Receiver{

    static int BUFFER_LENGTH=100;
    static int PORT=11500;
    public static void main(String[] args) throws SocketException {
        try{

            DatagramSocket socket = new DatagramSocket(PORT);

            byte[] buffer = new byte[BUFFER_LENGTH];
            DatagramPacket pack = new DatagramPacket(buffer, buffer.length);

            socket.receive(pack);

            byte[] data = pack.getData();
            String msg = new String(data);
            InetAddress fromAdd = pack.getAddress();
            int fromPort = pack.getPort();

            System.out.println(msg);
            System.out.println(fromAdd.getHostAddress());
            System.out.println(fromPort);

        } catch (Exception e){
            System.out.println(e.getMessage());
        }





    }
}