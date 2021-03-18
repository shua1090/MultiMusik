import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class controller {

    private SourceDataLine line = null;
    AudioFormat decodedFormat;
    AudioInputStream din;
    songPlayer pal;

    controller(String filename){
        File file = new File(filename);

        AudioInputStream in = null;
        try {
            in = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
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

    }


    private synchronized SourceDataLine getLine(AudioFormat audioFormat) {
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
        new controller("/home/shynn/IdeaProjects/multimusic/Sol Squadron - Ace Combat 7.mp3");
    }

}
