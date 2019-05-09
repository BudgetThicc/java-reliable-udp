package team.nameless.stp;

import java.net.*;

public class Receiver{

    static int BUFFER_LENGTH=10;
    static int PORT=8889;
    public static void main(String[] args) throws SocketException {

        try {
            DatagramSocket socket = new DatagramSocket(PORT, InetAddress.getByName("localhost"));
            while (true) {
                byte[] buffer = new byte[BUFFER_LENGTH];
                DatagramPacket pack = new DatagramPacket(buffer, buffer.length);

                socket.receive(pack);

                byte[] data = pack.getData();
                String msg = new String(data);
                InetAddress fromAdd = pack.getAddress();
                int fromPort = pack.getPort();

                System.out.println(msg);

            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println();
        }


    }
}