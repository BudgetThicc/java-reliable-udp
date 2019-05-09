package team.nameless.stp;

public class Window {
    int left=0;
    int right=0;
    byte[] data;
    private boolean[] sent;
    public int size;
    private int max;
    public Window(int max){
        this.max= max;
        data = new byte[max];
        sent = new boolean[max];
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
            sent[right]= false;
            right= (right+1)%max;
            size++;
        }
        else {
            System.out.println("队列已满");
        }
    }

    public void clear(int i){
        i=Math.abs(i);
        if(i>=size){
            left=0;
            right=0;
            size=0;
            sent = new boolean[max];
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

    public boolean isSent(int i){
        boolean element;
        if(!isEmpty()){
            element = sent[(left+i)%max];
            return element;
        } else {
            System.out.println("队列为空");
        }
        return false;
    }

    public void sent(int i){
        i=i%size;
        if(!isEmpty()){
            sent[(left+i)%max]=true;
        } else {
            System.out.println("队列为空");
        }
    }

    public void unSent(int i){
        i=i%size;
        if(!isEmpty()){
            sent[(left+i)%max]=false;
        } else {
            System.out.println("队列为空");
        }
    }

    public boolean isEmpty(){
        return size==0;
    }

    public boolean isFull(){
        return size==max;
    }

}
