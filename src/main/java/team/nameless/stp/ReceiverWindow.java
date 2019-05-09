package team.nameless.stp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Yang ShengYuan
 * @date 2019/5/8
 * @Description 一个用来实现接收方接收无序数据段的数据结构
 **/
public class ReceiverWindow {
    byte[] buffer;     // 内部数据结构，是个byte数组
    boolean[] isFilled;// 存对应byte数组的每个位置有没有放入数据
    public int startSeq; //缓存区起始位置应当存放的字节的序列号
    private int endSeq;   //缓存区结束位置应当存放的字节的序列号
    private FileOutputStream fileOutputStream;

    public ReceiverWindow(int length, String filename) throws IOException{//根据参数缓存区大小、写入文件名来构造
        this.buffer = new byte[length];
        this.isFilled = new boolean[length];
        this.startSeq = 0;
        this.endSeq = length - 1;

        //create file for storing
        File file = new File(filename);
        file.createNewFile();
        this.fileOutputStream = new FileOutputStream(filename);
    }

    public void setWindow(int startSeq){//建立连接时设定缓存区头尾的序号
        this.startSeq = startSeq;
        this.endSeq = this.startSeq+this.buffer.length-1;

    }

    public void insert(STPsegement rcvSegement){//插入一个STP数据段的data
        int seq = rcvSegement.getSeq();
        int index = seq - this.startSeq;
        try{
            for(int i=0;i<rcvSegement.getDataLength();i++, index++){
                buffer[index] = rcvSegement.getData()[i];
                isFilled[index] = true;
            }
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("startSeq: "+this.startSeq+" "+"seq:"+seq);
        }

    }

    public boolean canclean(){//判断是否可以写入并发送ACK
        return isFilled[0];//如果开头有数据那么就可以ACK了
    }

    public int cleanAndWrite() throws IOException{//将buffer的开头连续的部分写入，并返回最后一个字节的序号+1，并循环利用buffer的空间
        int index = 0;
        while(isFilled[index]){
            index++;
        }
        byte[] out = new byte[index];
        System.arraycopy(this.buffer,0,out,0,index);
        moveBufferToHead(index);
        fileOutputStream.write(out);
        this.startSeq+=index;
        this.endSeq+=index;

        return this.startSeq;
    }


    private void moveBufferToHead(int index){//clean之后移动窗口内容至最前
        System.arraycopy(this.buffer,index,this.buffer,0,this.buffer.length-index);
        System.arraycopy(this.isFilled,index,this.isFilled,0,this.buffer.length-index);
        for(int i=this.buffer.length-index;i<this.buffer.length;i++){
            this.buffer[i] = 0;
            this.isFilled[i] = false;
        }
    }


}
