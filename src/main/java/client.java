import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class client {

    songPlayer player;
    Socket sock;

    DataInputStream in = null;

    client(int port, String ipaddress){

//        new songPlayer();
        System.out.printf("Here %d and %s%n", port, ipaddress);

        try {
            sock = new Socket(ipaddress, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            var z = sock.getInputStream();
            in = new DataInputStream(z);
//            assert in != null;

            float aFloat = in.readFloat();
            System.out.println("aFloat = " + aFloat);

            int anInt = in.readInt();
            System.out.println("anInt = " + anInt);

            player = new songPlayer(aFloat, anInt);


            while (true){
                var stat = in.readInt();

                if (stat == 1){
//                    System.out.println("Waiting for bytes");

                    byte[] bar = new byte[4096];

                    in.readFully(bar, 0, bar.length);;
//                    System.out.println("Received bytes");
                    player.playBytes(bar);
                } else if (stat == -1){
                    break;
                } else {
                    System.out.println("ERROR");
                    System.exit(-1);
                }

                System.out.println("Playing");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        System.err.println("DESTRUCTING");
        super.finalize();
        sock.close();
    }

    public static void main(String[] args) {
        new client(Integer.parseInt(args[1]), args[0]);
    }



}
