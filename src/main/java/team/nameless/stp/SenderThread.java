package team.nameless.stp;

import java.io.IOException;
import java.net.*;


public class SenderThread implements Runnable{
    static int HEADER_LENGTH=10;

    byte[] inSeg;//seq,ack等应答数据的一个报文的保存器
    byte[] outSeg;//发送出去的报文
    Window outWindow;//等待发送的队列
    int windowSize;

    byte[] data;
    int dataPointer;

    static DatagramSocket socket;
    InetAddress toAdd;
    int toPort;

    int acked;
    int seq;
    int ack;

    SenderThread(int windowSize,int MSS,int port,String ip){
        inSeg=new byte[MSS+HEADER_LENGTH];
        outSeg=new byte[MSS];
        outWindow=new Window(windowSize);
        this.windowSize=windowSize;
        data="".getBytes();
        dataPointer=0;
        try {
            socket = new DatagramSocket(port,InetAddress.getByName(ip));
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e){
            e.printStackTrace();
        }

        seq=0;
        ack=0;
        acked=0;
    }

    public void setDes(String toAddStr,int toPort) {
        try {
            toAdd =InetAddress.getByName(toAddStr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.toPort=toPort;
    }


    public void setData(byte[] data){
        this.data=new byte[data.length];
        System.arraycopy(data,0,this.data,0,data.length);
    }

    private void initListener(){
        ListenerThread listener=new ListenerThread(this);
        Thread t=new Thread(listener);
        t.start();
    }

    private void loadWindow(){
        while(outWindow.size<windowSize&&dataPointer<data.length){
            outWindow.push(data[dataPointer]);
            dataPointer++;
        }
    }

    private void dumpWindow(int i){
        outWindow.clear(i);//清除前i个字节
        //todo:清除前i个计时器
        //todo:计时器，到点对window中数据unsent。

    }

    private void sendSeg(){
        try {
            sendSTPSeg(outSeg,false,false,seq,ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendSTPSeg(byte[] data, boolean isSYN, boolean isFIN , int seq, int ack) throws IOException {
        System.out.println(new String(data));
        STPsegement stpSegement = new STPsegement(data,isSYN,isFIN,seq,ack);
        DatagramPacket outPacket = new DatagramPacket(stpSegement.getByteArray(),
                stpSegement.getByteArray().length,toAdd,toPort);
        this.socket.send(outPacket);
        this.seq+=stpSegement.getDataLength();
        //for debugging.....
        System.out.println("Send: seq:"+seq+" "+"ack:"+ack);
        //for debugging.....
    }

    private void sendWindow(){
        int pointer=0;
        int segPointer=0;
        while(pointer<outWindow.size){//遍历outWindow
            while(outWindow.isSent(pointer)) {//跳过已发送的报文
                pointer++;
                if(pointer>=outWindow.size)
                    break;
            }
            if(pointer>=outWindow.size)
                break;

            outSeg[segPointer]=outWindow.get(pointer);//向下一个要发送的报文中推送数据

            outWindow.sent(pointer);//推送过的数据对应sent位标为true
            segPointer++;
            pointer++;
            if(segPointer>=outSeg.length){
                sendSeg();
                outSeg=new byte[outSeg.length];
                segPointer=0;
            }
        }
        if(segPointer!=0){//pointer遍历完而segPointer不为0，说明有最后一个碎片数据段
            sendSeg();
            outSeg=new byte[outSeg.length];
        }
    }

    public void callBackSeq(int newAck,int newSeq){
        dumpWindow(this.acked-newAck);//清空已收到部分
        this.acked=newAck;//调用callback会暂停SenderThread run方法，seq赋值完后继续。
        this.ack=newSeq+1;//ack为确认报文的seq+1
    }

    public void run(){
        initListener();
        while(socket!=null){
            loadWindow();
            sendWindow();
            if(dataPointer>=data.length&&outWindow.size==0){
                break;
                //todo:发送FIN
            }
        }
    }
}
