import javax.sound.sampled.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CyclicBarrier;

public class server {

    AudioFormat decodedFormat;
    AudioInputStream din;
    public static songPlayer pal;

    public CyclicBarrier latch;

    ArrayList<Thread> theList = new ArrayList<Thread>();
    ArrayList<sender> senderList = new ArrayList<>();

    server(String songPath) {
        File file = new File(songPath);

        AudioInputStream in = null;
        try {
            URL z = getClass().getResource(songPath);
            System.out.println(z);
            in = AudioSystem.getAudioInputStream(file
//                    new BufferedInputStream(getClass().getResourceAsStream(songPath))
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

        int i = 0;

        latch = new CyclicBarrier(1);

        System.out.println("Adding handlers");
        for (i = 0; i < 1; i++){

            Socket c = null;

            try {
                c = sock.accept();
                System.out.println("Accepted");
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert c != null;

            sender k = new sender(i, latch, c);

            k.sendSongData(decodedFormat.getSampleRate(), decodedFormat.getChannels());

//            theList.add(new Thread(k));
            senderList.add(k);
        }

        try {
            rawplay(din);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void updateHandlerQueue(byte[] bar){
        for (int i = 0; i < senderList.size(); i++){
            senderList.get(i).add(bar);
            var k = new Thread(senderList.get(i));
            k.start();

//            System.out.println("Number waiting: " + latch.getNumberWaiting());
//            System.out.println("Parties: " + latch.getParties());

            if (i == senderList.size() - 1){
//                System.out.println("Joining");
                try {
                    k.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

//        System.out.println("Number waiting: " + latch.getNumberWaiting());
//        System.out.println("Parties: " + latch.getParties());
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
        new server(args[0]);
    }

}
