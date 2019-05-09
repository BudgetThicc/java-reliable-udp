package team.nameless.stp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class ListenerThread implements Runnable{
    DatagramSocket socket;
    byte[] inSeg;
    SenderThread sender;
    int acked;

    ListenerThread(SenderThread sender){
        this.socket=sender.socket;
        this.inSeg=new byte[sender.inSeg.length];
        this.sender=sender;
        this.acked=0;
    }

    public void run() {
        while (true) {
            try {
                DatagramPacket rcvPacket = new DatagramPacket(this.inSeg, this.inSeg.length);
                this.socket.receive(rcvPacket);

                STPsegement rcvSTPsegement = new STPsegement(rcvPacket.getData());
                System.out.println("receive:  seq:"+rcvSTPsegement.getSeq()+" ack:"+rcvSTPsegement.getAck());
                int newAck=rcvSTPsegement.getAck();
                if(acked<newAck){
                    acked=newAck;
                    sender.callBackSeq(acked,rcvSTPsegement.getSeq());
                    System.out.println("acked:"+acked+"seq:"+rcvSTPsegement.getSeq());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
