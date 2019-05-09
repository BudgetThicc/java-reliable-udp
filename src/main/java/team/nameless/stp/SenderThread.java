package team.nameless.stp;

import java.io.IOException;
import java.net.*;


public class SenderThread implements Runnable{
    static int HEADER_LENGTH=10;
    static int WINDOW_SIZE=10;

    byte[] inSeg;//seq,ack等应答数据的一个报文的保存器
    byte[] outSeg;//发送出去的报文
    Window outWindow;//等待发送的队列

    byte[] data;
    int dataPointer;

    static DatagramSocket socket;
    InetAddress toAdd;
    int toPort;

    int seq;

    SenderThread(int MSS,int port,String ip){
        inSeg=new byte[MSS+HEADER_LENGTH];
        outSeg=new byte[MSS];
        outWindow=new Window(WINDOW_SIZE);

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
    }

    public void setDes(String toAddStr,int toPort) {
        try {
            toAdd =InetAddress.getByName(toAddStr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.toPort=toPort;
    }


    public void setData(String data){
        this.data=data.getBytes();
    }

    private void initListener(){
        ListenerThread listener=new ListenerThread(this);
        Thread t=new Thread(listener);
        t.start();
    }

    private void loadWindow(){
        while(outWindow.size<WINDOW_SIZE&&dataPointer<data.length){
            outWindow.push(data[dataPointer]);
            dataPointer++;
        }
    }

    private void dumpWindow(int i){
        outWindow.clear(i);//清除前i个字节
        //todo:清除前i个计时器

    }

    private void sendSeg(){
        DatagramPacket packet=new DatagramPacket(outSeg,outSeg.length,toAdd,toPort);
        try {
            socket.send(packet);
            System.out.println(new String(outSeg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void send(byte[] data, boolean isSYN, boolean isFIN , int seq, int ack) throws IOException {
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
        while(pointer<outWindow.size){
            outSeg[segPointer]=outWindow.get(pointer);
            segPointer++;
            pointer++;
            if(segPointer>=outSeg.length){
                sendSeg();
                outSeg=new byte[outSeg.length];
                segPointer=0;
            }
        }
        if(segPointer!=0){//有最后一个报文
            sendSeg();
            outSeg=new byte[outSeg.length];
        }
    }

    public void callBackSeq(int newSeq){
        dumpWindow(this.seq-newSeq);
        this.seq=newSeq;//调用callback会暂停SenderThread run方法，seq赋值完后继续。
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
