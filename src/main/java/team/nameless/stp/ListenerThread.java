package team.nameless.stp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ListenerThread implements Runnable{
    String RECEIVED="Number of ACKs received: ";

    DatagramSocket socket;
    byte[] inSeg;
    SenderThread sender;
    int acked;

    Logger logger;

    ListenerThread(SenderThread sender,Logger logger){
        this.socket=sender.socket;
        this.inSeg=new byte[sender.inSeg.length];
        this.sender=sender;
        this.acked=0;
        this.logger=logger;
    }

    public void run() {
        while (true) {
            DatagramPacket rcvPacket = new DatagramPacket(this.inSeg, this.inSeg.length);
            ready();
            try {
                this.socket.receive(rcvPacket);
            } catch (SocketException e) {
                System.out.println("Listener Closed");
                break;
                //socket被关闭，直接退出监听，socket若非正常关闭也由sender主线程处理
            } catch (IOException e) {
                e.printStackTrace();
            }

            STPsegement rcvSTPsegement = new STPsegement(rcvPacket.getData());
            System.out.println("receive:  seq:"+rcvSTPsegement.getSeq()+" ack:"+rcvSTPsegement.getAck());
            if(rcvSTPsegement.getSYN()) {
                handleNorm(rcvSTPsegement);
                handleSYN();
                logger.addLog("recv","SA",rcvSTPsegement.getSeq(),0,rcvSTPsegement.getAck());
            }else if(rcvSTPsegement.getFIN()){
                handleNorm(rcvSTPsegement);
                handleFIN();
                logger.addLog("recv","FA",rcvSTPsegement.getSeq(),0,rcvSTPsegement.getAck());
            }else {
                handleNorm(rcvSTPsegement);
                logger.addLog("recv","A",rcvSTPsegement.getSeq(),0,rcvSTPsegement.getAck());
            }
            logger.addAttr(RECEIVED,1);
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
    }

    private void handleFIN(){
        sender.callBackFIN(true);
    }

    private void ready(){
        sender.callBackReady();
    }
}
