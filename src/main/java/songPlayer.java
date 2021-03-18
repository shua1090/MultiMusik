import javax.sound.sampled.*;

public class songPlayer {

    AudioFormat decodedFormat;
    SourceDataLine line = null;

    private synchronized SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info =
                new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);

        return res;
    }

    songPlayer(float sampleRate, int channelAmount){
        decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16,
                channelAmount,
                channelAmount * 2,
                sampleRate,
                false);

        try {
            line = getLine(decodedFormat);
            line.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    void playBytes(byte[] arr){
        line.write(arr, 0, 4096);
    }

}
