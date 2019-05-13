package team.nameless.stp;

public class TimerThread implements Runnable {
    int ms;
    Window window;
    TimerThread(int ms, Window window){
        this.ms=ms;
        this.window=window;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {//如果真有东西打断计时器那估计就是主线程了，所以直接断开线程
                System.out.println("Timer Closed");
                break;
            }
            for(int i=0;i<window.getMax();i++){
                int temp=window.getDelay(i);
                if(temp!=0)
                    window.setDelay(i,temp-1);//减去一单位计时
            }
        }
    }
}
