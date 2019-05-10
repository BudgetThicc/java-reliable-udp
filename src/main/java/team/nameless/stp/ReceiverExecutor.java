package team.nameless.stp;

import java.io.IOException;
import java.net.*;

/**
 * @author Yang ShengYuan
 * @date 2019/5/6
 * @Description ${DESCRIBE}
 **/
public class ReceiverExecutor {
    //param
    private int receiver_port;//接收方端口
    private String filename;  //需要存的文件路径

    //other need attributes
    private int MSS = 24;     //暂时先写着MSS
    private DatagramSocket udpSocket;
    private String receiver_ip = "localhost";
    private String sender_ip; //这个会根据收到的报文读出来
    private int sender_port;  //这个会根据收到的报文读出来
    private boolean isSYNed = false; //表示是否SYN了
    private int seq = 200; //receiver发送报文段的序号
    private int ack = 0;   //receiver发送报文段的ack值
    private byte[] InAndOutSTP; //用来构造udp报文的工具数组
    private ReceiverWindow window; //接收方缓存区数据结构

    //const
    private static String INPUT_ERROR_MSG = "usage: java Receiver <receiver_port> <file.txt>";
    private static int STPheaderSize = 10; //接收方知道header的大小

    public ReceiverExecutor(String args[]) throws IOException {
        //read params
        if (args.length != 2) {
            System.out.println(INPUT_ERROR_MSG);
        }
        try {
            this.receiver_port = Integer.parseInt(args[0]);
            this.filename = args[1];
        } catch (Exception e) {
            System.out.println(INPUT_ERROR_MSG);
        }
        this.init();//init sth
    }

    public void init() throws IOException {
        //init socket
        this.udpSocket = new DatagramSocket(this.receiver_port,InetAddress.getByName(this.receiver_ip));
        //init window
        this.window = new ReceiverWindow(200000,this.filename);
    }

    public  void go() throws IOException {

        getConnection();

        while(true){
            InAndOutSTP = new byte[this.MSS+STPheaderSize];
            DatagramPacket rcvPacket = new DatagramPacket(InAndOutSTP,InAndOutSTP.length);
            this.udpSocket.receive(rcvPacket);
            this.sender_ip = rcvPacket.getAddress().getHostName();
            this.sender_port = rcvPacket.getPort();
            STPsegement stpSegement= new STPsegement(InAndOutSTP);
            System.out.println();
            ///////////////////////////////
            System.out.println("receive:  seq:"+stpSegement.getSeq()+" ack:"+stpSegement.getAck());
            ///////////////////////////////
            if(seq==197){
                for(byte b:InAndOutSTP){
                    System.out.print(b+" ");
                }
            }
            if(stpSegement.getFIN()){//如果是终止连接报文，进入终止方法
                killConnection(stpSegement);
                break;
            }

            this.window.insert(stpSegement);//否则将收到的报文交给接收方缓存区window处理
            if(this.window.canclean()){//判断是否收到连续的报文需要发送ACK
                this.ack = this.window.cleanAndWrite();
                this.send(new byte[0],false,false,this.seq,this.ack);//发送ACK
            }
        }
        this.udpSocket.close();

    }

    public void getConnection() throws IOException {//建立三次握手连接
        InAndOutSTP = new byte[STPheaderSize];//get the first and third handshake segement
        DatagramPacket rcvPacket = new DatagramPacket(InAndOutSTP,InAndOutSTP.length);

        while (true){//get the first handshake
            this.udpSocket.receive(rcvPacket);
            STPsegement stpSegement= new STPsegement(InAndOutSTP);

            ///////////////////////////////
            System.out.println("receive:  seq:"+stpSegement.getSeq()+" ack:"+stpSegement.getAck());
            ///////////////////////////////

            if(stpSegement.getSYN()){
                this.ack = stpSegement.getSeq()+1;
                this.sender_ip = rcvPacket.getAddress().getHostName();
                this.sender_port = rcvPacket.getPort();
                break;
            }
        }

        this.send(new byte[0], true, false,this.seq,this.ack);//sendSYN the second handshake
        this.udpSocket.receive(rcvPacket);//get the third handshake

        ///////////////////////////////
        System.out.println("receve:  seq:"+(new STPsegement(InAndOutSTP)).getSeq()+" ack:"+(new STPsegement(InAndOutSTP)).getAck());
        ///////////////////////////////

        this.isSYNed = true;
        this.window.setWindow((new STPsegement(InAndOutSTP)).getSeq());

    }

    public void killConnection(STPsegement FINsegement) throws IOException {//终止连接
        this.ack = FINsegement.getSeq()+1;
        send(new byte[0],false,true,this.seq,this.ack);
        DatagramPacket endPacket = new DatagramPacket(InAndOutSTP,InAndOutSTP.length);
        this.udpSocket.receive(endPacket);
        System.out.println("end");
        this.udpSocket.close();
    }

    public void send(byte[] data, boolean isSYN, boolean isFIN , int seq, int ack) throws IOException {//发送报文
        STPsegement stpSegement = new STPsegement(data,isSYN,isFIN,seq,ack);
        DatagramPacket outPacket = new DatagramPacket(stpSegement.getByteArray(),
                stpSegement.getByteArray().length,InetAddress.getByName(this.sender_ip),this.sender_port);
        this.udpSocket.send(outPacket);
        this.seq+=1;//receiver每次发送必定只消耗自己一个seq

        System.out.println("Send: seq:"+seq+" "+"ack:"+ack);

    }
}
