import javax.swing.*;

public class Main extends JFrame {

    private JButton play;
    private JButton pause;
    private JButton resume;
    private JButton stop;

    private Sound player;


    public static void main(String[] args) {
//        Sound player = new Sound("/home/rbencheikh/Documents/repo/SoundThread/src/sound.wav");
        new WavPlayer();

    }
}
