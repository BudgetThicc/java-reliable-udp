package team.nameless.stp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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
            DatagramPacket rcvPacket = new DatagramPacket(this.inSeg, this.inSeg.length);
            ready();
            try {
                this.socket.receive(rcvPacket);
            } catch (SocketException e) {
                break;
                //socket被关闭，直接退出监听，socket若非正常关闭也由sender主线程处理
            } catch (IOException e) {
                e.printStackTrace();
            }

            STPsegement rcvSTPsegement = new STPsegement(rcvPacket.getData());
            System.out.println("receive:  seq:"+rcvSTPsegement.getSeq()+" ack:"+rcvSTPsegement.getAck());
            if(rcvSTPsegement.getSYN()) {
                handleSYN();
            }else if(rcvSTPsegement.getFIN()){
                handleFIN();
            }else {
                handleNorm(rcvSTPsegement);
            }

        }
    }

    private void handleNorm(STPsegement rcvSTPsegement){
        int newAck=rcvSTPsegement.getAck();
        if(acked<newAck){
            acked=newAck;
            sender.callBackSeq(acked,rcvSTPsegement.getSeq());
            System.out.println("CallBack: acked:"+acked+" seq:"+rcvSTPsegement.getSeq());
        }
    }

    private void handleSYN(){
        sender.callBackSYN(true);
        System.out.println(true);
    }

    private void handleFIN(){
        sender.callBackFIN(true);
    }

    private void ready(){
        sender.callBackReady();
    }
}
