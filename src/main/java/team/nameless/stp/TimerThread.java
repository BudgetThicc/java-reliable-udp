package team.nameless.stp;

public class TimerThread implements Runnable {
    int ms;
    long prev;
    Window window;
    Logger logger;
    TimerThread(int ms, Window window,Logger logger){
        this.ms=ms;
        this.window=window;
        this.logger=logger;
    }

    @Override
    public void run() {
        prev=System.currentTimeMillis();
        while(true) {
            try {
                Thread.sleep(1);
//                System.out.println(System.currentTimeMillis());
                long current=System.currentTimeMillis();
                while(current-prev>ms) {
                    prev+=ms;
                    for (int i = 0; i < window.getMax(); i++) {
                        int temp = window.getDelay(i);
                        if (temp != 0) {
                            window.setDelay(i, temp - 1);//减去一单位计时
                        }
                    }
                }
            } catch (InterruptedException e) {//如果真有东西打断计时器那估计就是主线程了，所以直接断开线程
                System.out.println("Timer Closed");
                break;
            }
        }
    }
}
