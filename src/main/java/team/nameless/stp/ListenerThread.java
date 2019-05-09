package team.nameless.stp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class ListenerThread implements Runnable{
    DatagramSocket socket;
    byte[] inSeg;
    SenderThread sender;
    int seq;

    ListenerThread(SenderThread sender){
        this.socket=sender.socket;
        this.inSeg=sender.inSeg;
        this.sender=sender;
        this.seq=0;
    }

    public void run() {
        while (true) {
            inSeg[0]++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sender.callBackSeq(seq);
            try {
                DatagramPacket rcvPacket = new DatagramPacket(this.inSeg, this.inSeg.length);
                try {
                    this.socket.receive(rcvPacket);
                } catch (SocketTimeoutException e) {
//                        e.printStackTrace();
                    continue;
                }
                STPsegement rcvSTPsegement = new STPsegement(rcvPacket.getData());
                System.out.println("receive:  seq:"+rcvSTPsegement.getSeq()+" ack:"+rcvSTPsegement.getAck());
                int newSeq=rcvSTPsegement.getAck();
                if(seq<newSeq){
                    seq=newSeq;
                    sender.callBackSeq(seq);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
