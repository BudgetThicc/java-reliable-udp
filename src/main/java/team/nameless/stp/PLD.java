package team.nameless.stp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

public class PLD {
    static Random random;
    static double pdrop;
    static DatagramSocket socket;
    static Logger logger;

    static String DROPPED="Number of data segments DROPPED: ";

    static void setRandom(long seed){
        random=new Random(seed);
    }

    static void setPdrop(double _pdrop){
        pdrop=_pdrop;
    }

    static void setSocket(DatagramSocket _socket){
        socket=_socket;
    }

    static void setLogger(Logger _logger){logger=_logger;}

    static void send(DatagramPacket packet) throws IOException {//由于UDP不包含ACK信息，先拆箱查看ACK判断是否概率丢弃
        STPsegement STPseg = new STPsegement(packet.getData());
        if(STPseg.getFIN()||STPseg.getSYN()) {
            socket.send(packet);
        }else{
            if(random.nextDouble()>pdrop){
                socket.send(packet);
            }else{
                logger.addLog("drop","D",STPseg.getSeq(),STPseg.getDataLength(),STPseg.getAck());
                logger.addAttr(DROPPED,1);
            }
        }
    }

}
