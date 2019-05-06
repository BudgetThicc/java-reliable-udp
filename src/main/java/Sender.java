import java.net.*;

public class AppUDP1 {

    public static void main(String[] args) throws SocketException {
        try{
            DatagramSocket socket = new DatagramSocket(10000);

            String msg = "test";
            byte[] msg_buf = msg.getBytes();
            int msg_size = msg_buf.length;
            InetAddress destination_address = InetAddress.getLocalHost();
            int destination_port = 10000;

            DatagramPacket pack = new DatagramPacket(msg_buf, msg_size, destination_address, destination_port);
            socket.send(pack);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}