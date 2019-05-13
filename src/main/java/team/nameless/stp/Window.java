package team.nameless.stp;

public class Window {
    int left=0;
    int right=0;
    byte[] data;
    private int[] delay;
    public int size;
    private final int max;//按作业要求不实现可变窗口，故为了保证安全性max设为final
    public Window(int max){
        this.max= max;
        data = new byte[max];
        delay = new int[max];//初始化发送延迟为0，即socket取到后立即发送
        left=0;
        right=0;
        size=0;
    }

    public byte poll(){
        byte element = -1;
        if(!isEmpty()){
            element = data[left];
            left =(left+1)%max;
            size--;
            return element;
        }
        else {
            System.out.println("队列为空");
            return -1;
        }
    }

    public void push(byte element){
        if(!isFull()){
            data[right] = element ;
            delay[right]= 0;
            right= (right+1)%max;
            size++;
        }
        else {
            System.out.println("队列已满");
        }
    }

    public void clear(int i){//收到ack，window移动，数据退栈
        //todo：window的剩余delay值计算得出新RTT
        i=Math.abs(i);
        if(i>size){
            left=0;
            right=0;
            size=0;
            delay = new int[max];
        }else{
            left=(left+i)%max;
            size-=i;
        }
    }

    public byte get(int i){
        byte element=0;
        if(!isEmpty()){
            element = data[(left+i)%max];
            return element;
        } else {
            System.out.println("队列为空");
        }
        return element;
    }

    public boolean canSent(int i){
        boolean element;
        if(!isEmpty()){
            element = delay[(left+i)%max]==0;
            return element;
        } else {
            System.out.println("队列为空");
        }
        return false;
    }

    public void setDelay(int i,int RTT){
        if(size==0) return;
        i=i%size;
        if(!isEmpty()){
            delay[(left+i)%max]=RTT;
        } else {
            System.out.println("队列为空");
        }
    }

    public int getDelay(int i){
        if(size==0) return 0;
        i=i%size;
        if(!isEmpty()){
            return delay[(left+i)%max];
        } else {
            System.out.println("队列为空");
            return 0;
        }
    }


    public boolean isEmpty(){
        return size==0;
    }

    public boolean isFull(){
        return size==max;
    }

    public int getMax(){return max;}
}
