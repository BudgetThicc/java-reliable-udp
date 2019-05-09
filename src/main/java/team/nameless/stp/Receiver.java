package team.nameless.stp;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Yang ShengYuan
 * @date 2019/5/6
 * @Description 驱动ReceiverExecutor执行的类
 **/
public class Receiver {
    public static void main(String args[]){
        ReceiverExecutor re = null;
        try {
            re = new ReceiverExecutor(args);//把参数传递给执行类
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            re.go();//启动
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
