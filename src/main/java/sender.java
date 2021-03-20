import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;

public class sender implements  Runnable{

    Socket sock;
    BlockingQueue<byte[]> threadQueue = new LinkedBlockingQueue<>();
    int stat;

    public DataOutputStream out;

    CyclicBarrier theLock;

    sender(int i, CyclicBarrier barrier, Socket theSocket){
        stat = i;
        theLock = barrier;
        sock = theSocket;
    }

    void sendSongData(float first, int second){
        try {
            out = new DataOutputStream(sock.getOutputStream());

            out.writeFloat(first);
            out.writeInt(second);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    sender(Socket s){
        sock = s;
    }

    synchronized void add(byte[] bar){
        threadQueue.add(bar);
    }

    @Override
    protected void finalize() {

        System.err.println("DESTRUCTING");

        try {

            out.writeInt(-1);

//            super.finalize();
            sock.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        System.exit(-1);

    }

    @Override
    public void run() {
//        System.out.println("Running");
        try {
            theLock.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        if (!threadQueue.isEmpty()){
//            System.out.println("Thread: " + Thread.currentThread().getName() + " " + Thread.currentThread().getId());
//            server.pal.playBytes(threadQueue.poll());
            try {

                if (threadQueue.peek() != null) {
                    out.writeInt(1);
                    System.out.println("Wrote Integer");

                    out.write(threadQueue.poll());
                    System.out.println("Wrote Byte array");

                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}
