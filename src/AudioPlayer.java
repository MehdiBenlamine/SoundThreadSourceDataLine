import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingWorker;

public class AudioPlayer extends SwingWorker<Void, byte[]> {
    private final File audioFile;
    private final BlockingQueue<byte[]> queue;
    private SourceDataLine line;
    private boolean playing;
    private boolean paused;
    private boolean stopped;
    private boolean closed;
    private AudioPlayerDemo.SoundPanel soundPanel;
    private double[] audioData;
    public byte[] audioDataBytes;
    private Thread playingThread;
    
    public AudioPlayer(File audioFile, AudioPlayerDemo.SoundPanel soundPanel) {
        this.audioFile = audioFile;
        this.queue = new LinkedBlockingQueue<>();
        this.playing = false;
        this.paused = false;
        this.soundPanel = soundPanel;
        audioData = read(audioFile);
        try {
			audioDataBytes = getSoundData(audioFile);
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
    }
    
    public static byte[] getSoundData(File audioFile) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);

        int bufferSize = (int) (audioInputStream.getFrameLength() * audioInputStream.getFormat().getFrameSize());
        byte[] buffer = new byte[bufferSize];

        int bytesRead = 0;
        int totalBytesRead = 0;
        while ((bytesRead = audioInputStream.read(buffer, totalBytesRead, buffer.length - totalBytesRead)) != -1) {
            totalBytesRead += bytesRead;
            if (totalBytesRead == buffer.length) {
                break;
            }
        }

        return buffer;
    }
    
    public static double[] read(File filename) {
    	byte[] data = new byte[0];
		try {
			data = getSoundData(filename);
		} catch (IOException | UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        int N = data.length;
        double[] d = new double[N/2];
        for (int i = 0; i < N/2; i++) {
            d[i] = ((short) (((data[2*i+1] & 0xFF) << 8) + (data[2*i] & 0xFF))) / ((double) Short.MAX_VALUE);
        }
        return d;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat audioFormat = audioInputStream.getFormat();
        int bufferSize = audioFormat.getFrameSize() * Math.round(audioFormat.getSampleRate() / 10);
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        try {
            line = getLine(audioFormat);
            line.start();
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                queue.put(chunk);
                if (paused) {
                    line.stop();
                    synchronized (this) {
                        while (paused) {
                            wait();
                        }
                    }
                    line.start();
                }
            }
            queue.put(new byte[0]); // end of stream
            line.drain();
            line.stop();
        } catch (IOException | InterruptedException | LineUnavailableException e) {
            e.printStackTrace();
        } finally {
            if (line != null) {
                line.close();
            }
        }
        return null;
    }
    
    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
        return line;
    }
    
    public synchronized void play() {
        if (!playing) {
            playing = true;
            playingThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[2048];
                    while (true) {
                    	synchronized(this) {
                            while (this.paused) {
                                wait();
                            }
                        }
                        byte[] chunk = queue.take();
                        if (chunk.length == 0) {
                            break;
                        }
                        
                        publish(chunk);
                        int offset = 0;
                        while (offset < chunk.length) {
                        	
                            int len = Math.min(buffer.length, chunk.length - offset);
                            System.arraycopy(chunk, offset, buffer, 0, len);
                            if (!paused) {
                            line.write(buffer, 0, len);
                            }
                            offset += len;
                            
                        }
                    }
                } catch (InterruptedException e) {
                	Thread.currentThread().interrupt();
                } finally {
                	System.out.println("FINALLY... ");
                    line.drain();
                    line.stop();
                    line.close();
                    line = null;
                    playing = false;
                    closed = true;
                    queue.clear();
                    synchronized (this) {
                        notify();
                    }
                }
            });
            playingThread.start();
        }
    }
    
    public synchronized void pause() {
        if (playing && !paused) {
            paused = true;
        }
    }
    
    public synchronized void resume() {
        if (playing && paused) {
            synchronized (playingThread) {
            	paused = false;
                notify();
            }
            System.out.println("REPRISE RESUME");
        }
    }
    
    @Override
    protected void process(List<byte[]> chunks) {
        byte[] chunk = chunks.get(chunks.size() - 1);
        if(line != null) {
	        soundPanel.curIndexPosGUI = line.getLongFramePosition();
	    	soundPanel.updateSoundData(audioData);
        }
    }
    
    public synchronized void stop() {
        if (playing) {
            line.stop();
            line.close();
            playing = false;
            paused = false;
            closed = true;
            queue.clear();
            synchronized (this) {
                notify();
            }
        }
    }
}

