package team.nameless.stp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Logger implements Runnable {
    File logFile;

    StringBuilder log;

    HashMap<String,Integer> attributes;

    Long startTime;

    Logger(String filename){
        startTime=System.currentTimeMillis();
        logFile=new File(filename);
        log=new StringBuilder();
        attributes=new HashMap<>();
        try{
            if(!logFile.exists()){
                logFile.createNewFile();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void addAttr(String key,Integer value){
        attributes.put(key,attributes.getOrDefault(key,0)+value);
    }

    public int getAttr(String key){
        return attributes.getOrDefault(key,0);
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(100);//每100ms更新一次日志内容
            } catch (InterruptedException e) {
                break;
            }
            write();
        }
    }

    public void addLog(String logType,String packType,int seq,int bytes,int ack){
        double time=(System.currentTimeMillis()-startTime)/1000.0;
        log.append(logType+" "+time+" "+packType+" "+seq+" "+bytes+" "+ack+"\n");
    }

    public void addLog(String str){
        log.append(str+"\n");
    }

    public void addLog_Attr(String key){
        log.append(key+attributes.getOrDefault(key,-1)+"\n");
    }

    public void write(){
        try {
            FileOutputStream out=new FileOutputStream(logFile,false);
            out.write(log.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
