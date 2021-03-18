import javax.sound.sampled.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Handler;

public class server {

    AudioFormat decodedFormat;
    AudioInputStream din;
    public static songPlayer pal;

    static byte[] visibleData = new byte[4096];

    Queue<byte[]> semaphore = new LinkedList<byte[]>();

    public CyclicBarrier latch;

    ArrayList<Thread> theList = new ArrayList<Thread>();
    ArrayList<handler> handlerList = new ArrayList<>();

    server(String songPath) {
//        File file = new File(songPath);

        AudioInputStream in = null;
        try {
            URL z = getClass().getResource(songPath);
            System.out.println(z);
            in = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(getClass().getResourceAsStream(songPath))
            );
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        assert in != null;
        AudioFormat baseFormat = in.getFormat();

        decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        din = AudioSystem.getAudioInputStream(decodedFormat, in);
        pal = new songPlayer(baseFormat.getSampleRate(), baseFormat.getChannels());


        ServerSocket sock = null;
        try {
            sock = new ServerSocket(5002);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert sock != null;

        System.out.println("Entering loop");

//        var pine = getLine(decodedFormat);
        new Thread(()->{
            try {
                rawplay(din);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        int i = 0;

        latch = new CyclicBarrier(1);

        System.out.println("Adding handlers");
        for (i = 0; i < 1; i++){
            handler k = new handler(i, latch);
            theList.add(new Thread(k));
            handlerList.add(k);
        }


//        var k = new handler();
//        theList.add(k);
//
//        var z = new handler();
//        theList.add(z);


    }

    void updateHandlerQueue(byte[] bar){
        for (int i = 0; i < handlerList.size(); i++){
            handlerList.get(i).add(bar);
            var k = new Thread(handlerList.get(i));
            k.start();

            System.out.println("Number waiting: " + latch.getNumberWaiting());
            System.out.println("Parties: " + latch.getParties());

            if (i == handlerList.size() - 1){
                System.out.println("Joining");
                try {
                    k.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        System.out.println("Number waiting: " + latch.getNumberWaiting());
        System.out.println("Parties: " + latch.getParties());

    }


    private void rawplay(AudioInputStream din) throws IOException {
        byte[] data = new byte[4096];

//        SourceDataLine line = getLine(targetFormat);
        int nBytesRead;

        do {
            nBytesRead = din.read(data, 0, data.length);
            updateHandlerQueue(data);
//            pal.playBytes(data);
//            semaphore.add(data.clone());
//            visibleData = data;
//            System.out.println(Arrays.toString(visibleData));
//            System.out.println()
//            pal.playBytes(visibleData);
//            System.out.println("Thread "+ Thread.currentThread().getName() + " is still going strong!");
        } while (nBytesRead != -1);

        din.close();
    }

    private SourceDataLine getLine(AudioFormat audioFormat) {
        SourceDataLine res = null;
        DataLine.Info info =
                new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            res = (SourceDataLine) AudioSystem.getLine(info);
            res.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return res;
    }

    public static void main(String[] args) {
        new server("Sol Squadron - Ace Combat 7.mp3");
    }

}
