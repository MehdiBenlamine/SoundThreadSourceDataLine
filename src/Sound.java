import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

/**
 *
 * @author Rafiul
 */
public class Sound {

    private Clip clip;
    public volatile boolean running,looping;

    int currentFrame = 0;
    public Sound(String location){
        looping = running = false;
        try {
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(location)));
//            runCursor();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void play(WavPanel panel) {
        running = true;
        if(!isResumed()) {
            clip.setFramePosition(0);
        }
        new Thread(new Runnable(){
            public void run(){
                clip.start();
                while(true){
                    panel.setCurrentIndex(clip.getFramePosition());

                    panel.repaint();
                    if(clip.getMicrosecondPosition() >= clip.getMicrosecondLength())
                        break;
                    if(!running)
                        break;
                }
            }
        }).start();
    }

    public void runCursor(JPanel panel) {
        new Thread(() -> {
            while (isRunning()) {
                int currentFrame = clip.getFramePosition();
                int totalFrames = clip.getFrameLength();
                int value = (int) (100.0 * currentFrame / totalFrames);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean state) {
        this.running = false;
    }
    public void pause(WavPanel panel){
        running = false;
        clip.stop();
        currentFrame = clip.getFramePosition();
    }
    public void resume(WavPanel panel){
        running = true;
        new Thread(new Runnable(){
            public void run(){
                clip.setFramePosition(currentFrame);
                panel.setCurrentIndex(currentFrame);
                clip.start();
                while(true){
                    panel.setCurrentIndex(clip.getFramePosition());
                    panel.repaint();
                    if(clip.getMicrosecondPosition() == clip.getMicrosecondLength())
                        break;
                    if(!running)
                        break;
                }
            }
        }).start();
    }
    public void stop(WavPanel panel){
        running = false;
        looping = false;
        clip.stop();
        clip.setFramePosition(0);
    }
    public void loop(){
        looping = true;
        new Thread(new Runnable(){
            public void run(){
                clip.start();
                while(looping){
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
            }
        }).start();
    }
    public boolean isResumed(){
        if(clip.getMicrosecondPosition()>0)
            return true;
        return false;
    }

    public void setClip(Clip audioClip) {
        this.clip = audioClip;
    }

    public Clip getClip() {
        return clip;
    }
}