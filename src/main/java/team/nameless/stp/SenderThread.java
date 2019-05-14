package team.nameless.stp;

import java.io.IOException;
import java.net.*;


public class SenderThread implements Runnable{
    static int HEADER_LENGTH=10;

    byte[] inSeg;//seq,ack等应答数据的一个报文的保存器
    byte[] outSeg;//发送出去的报文
    Window outWindow;//等待发送的队列
    int MWS;

    byte[] data;
    int dataPointer;

    DatagramSocket socket;
    InetAddress toAdd;
    int toPort;

    int acked;
    int seq;
    int ack;
    int timeout=20;//单位为0.1秒，因timer执行精度为0.1秒

    Thread listenerThread;
    Thread timerThread;

    boolean isFIN,isSYN,readyListen;

    SenderThread(int MWS,int MSS,int port,String ip,int timeout){
        inSeg=new byte[MSS+HEADER_LENGTH];
        outSeg=new byte[MSS];
        outWindow=new Window(MWS);
        this.MWS=MWS;
        this.timeout=timeout/100;

        data="".getBytes();
        dataPointer=0;
        try {
            socket = new DatagramSocket(port,InetAddress.getByName(ip));
            PLD.setSocket(socket);
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e){
            e.printStackTrace();
        }

        seq=100;
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
        listenerThread=new Thread(listener);
        listenerThread.start();
    }

    private void initTimer(){
        TimerThread timer=new TimerThread(100,outWindow);
        timerThread=new Thread(timer);
        timerThread.start();
    }

    private void loadWindow(){
        while(outWindow.size<MWS&&dataPointer<data.length){
            outWindow.push(data[dataPointer],this.seq);//将数据读入window的同时为其编号seq
            this.seq++;
            dataPointer++;
        }
    }

    private void dumpWindow(int i){
        outWindow.clear(i);//清除前i个字节
    }

    private void sendSeg(int seqOfSeg){
        try {
            sendSTPSeg(outSeg,false,false,seqOfSeg,ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSTPSeg(byte[] data, boolean isSYN, boolean isFIN , int seq, int ack) throws IOException {
        STPsegement stpSegement = new STPsegement(data,isSYN,isFIN,seq,ack);
        DatagramPacket outPacket = new DatagramPacket(stpSegement.getByteArray(),
                stpSegement.getByteArray().length,toAdd,toPort);
        PLD.send(outPacket);//sender发出，经过PLD
        //for debugging.....
        System.out.println("Send: seq:"+seq+" "+"ack:"+ack);
        System.out.println();
        //for debugging.....
    }

    private void sendWindow(){
        int pointer=0;
        int segPointer=0;
        int seqOfSeg=-1;//一个报文的seq，-1则为未采集状态，应从window中对应位置字节得到seq
        while(pointer<outWindow.size){//遍历outWindow
            while(!outWindow.canSent(pointer)) {//跳过还不可发送的报文
                pointer++;
                if(pointer>=outWindow.size)
                    break;
            }
            if(pointer>=outWindow.size)
                break;

            outSeg[segPointer]=outWindow.get(pointer);//向下一个要发送的报文中推送数据
            if(seqOfSeg==-1){seqOfSeg=outWindow.getSeq(pointer);}//若所推送字节为第一个字节，将报文seq设为其seq

            outWindow.setDelay(pointer,timeout);//推送过的数据对应sent位标为true
            segPointer++;
            pointer++;
            if(segPointer>=outSeg.length){
                sendSeg(seqOfSeg);
                outSeg=new byte[outSeg.length];
                segPointer=0;
                seqOfSeg=-1;
            }
        }
        if(segPointer!=0){//pointer遍历完而segPointer不为0，说明有最后一个碎片数据段
            sendSeg(seqOfSeg);
            outSeg=new byte[outSeg.length];
        }
    }

    public void callBackSeq(int newAck,int newSeq){
        dumpWindow(this.acked-newAck);//清空已收到部分
        this.acked=newAck;//调用callback会暂停SenderThread run方法，seq赋值完后继续。
        this.ack=newSeq+1;//ack为确认报文的seq+1
    }

    public void callBackSYN(boolean b){
        this.isSYN=b;
    }

    public void callBackFIN(boolean b){
        this.isFIN=b;
    }

    public void callBackReady(){
        this.readyListen=true;
    }

    private void establish(){
        try {
            this.sendSTPSeg(new byte[0], true, false, this.seq, 0);//send SYN the first handshake
            this.seq++;//因为还不涉及到window，手动增加seq
            System.out.print("Handshaking:");
            while (true) {
                System.out.print(".");
                if (this.isSYN) {//等待listener回调
                    this.sendSTPSeg(new byte[0], false, false, this.seq, this.ack);//send the third handshake
                    System.out.println();
                    break;
                }
            }
            System.out.println("////////////SYNed////////////");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            sendSTPSeg(new byte[0],false,true,this.seq,this.ack); //F
            this.seq++;//不涉及window，手动增加seq
            System.out.print("Handshaking:");
            while(true){//等待listener回调
                System.out.print(".");
                if(this.isFIN){//get F+A
                    this.sendSTPSeg(new byte[0],false,false,this.seq,this.ack);
                    System.out.println();
                    break;
                }
            }
            this.socket.close();
            System.out.println("////////////FINed////////////");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        initListener();
        while(!readyListen){};//等待listener准备完成
        establish();//握手完成前不发生drop
        initTimer();
        while(socket!=null){
            loadWindow();
            sendWindow();
            if(dataPointer>=data.length&&outWindow.size==0){
                close();//该操作将关闭Listener线程
                timerThread.interrupt();
                break;
            }
        }
    }
}
