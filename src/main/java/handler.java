import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

public class handler implements Runnable{

    Socket sock;
    BlockingQueue<byte[]> threadQueue = new LinkedBlockingQueue<>();
    int stat;

    CyclicBarrier theLock;

    handler(int i, CyclicBarrier the){
        stat = i;
        theLock = the;
    }

    handler(Socket s){
        sock = s;
    }

    synchronized void add(byte[] bar){
        threadQueue.add(bar);
    }

    @Override
    public void run() {
        System.out.println("Running");
        try {
            theLock.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        if (!threadQueue.isEmpty()){
            System.out.println("Thread: " + Thread.currentThread().getName() + " " + Thread.currentThread().getId());
            server.pal.playBytes(threadQueue.poll());
        }

    }
}
