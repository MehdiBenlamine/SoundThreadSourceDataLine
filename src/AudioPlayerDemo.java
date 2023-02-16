import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class AudioPlayerDemo extends JFrame {
	
    private static final String AUDIO_FILE = "C:\\Users\\mbenlamine\\Downloads\\Pulses-Biologic.wav"; //"C:\\Users\\mbenlamine\\Downloads\\vocal.wav"
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 200;
    
    private AudioPlayer audioPlayer;
    SoundPanel soundPanel;
    
    private JButton playButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton stopButton;
    private double[] currentSoundData;
    
    public AudioPlayerDemo() {
        super("Audio Player Demo");
        
        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        resumeButton = new JButton("Resume");
        stopButton = new JButton("Stop");
        
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (audioPlayer == null) {
                    audioPlayer = new AudioPlayer(new File(AUDIO_FILE), soundPanel);
                    audioPlayer.execute();
                }
                audioPlayer.play();
            }
        });
        
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (audioPlayer != null) {
                    audioPlayer.pause();
                }
            }
        });
        
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (audioPlayer != null) {
                    audioPlayer.resume();
                }
            }
        });
        
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (audioPlayer != null) {
                    audioPlayer.stop();
                    audioPlayer = null;
                }
            }
        });
        
        setSize(800, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        soundPanel = new SoundPanel();
        soundPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        
        add(soundPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resumeButton);
        buttonPanel.add(stopButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    
    class SoundPanel extends JPanel {
        private double[] soundData;
        public long curIndexPosGUI = 0;
        
        public SoundPanel() {
            setBackground(Color.WHITE);
        }
        
        public void updateSoundData(double[] soundData) {
            this.soundData = soundData;
            repaint();
        }
        
        public void paintComponent(Graphics g) 
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            if(soundData != null) {
                int segmentSize = soundData.length / getWidth();
                int curIndex = 0;
                double curVal = 0;
                for (int x = 0; x < getWidth(); x++) {
                    int y = (int)(getHeight() * (0.5 - curVal));
                    g2d.drawLine(x, y, x, getHeight() - y);
                    curIndex += segmentSize;
                    curVal = soundData[curIndex];
                }
            }
            
            if (soundData != null && audioPlayer != null && isPlaying()) {
		    	curIndexPosGUI = (curIndexPosGUI-1) * this.getWidth() / soundData.length;
		    	g.setColor(Color.RED);
	    		g2d.drawLine((int)curIndexPosGUI, 0, (int)curIndexPosGUI, getHeight());
            }
        }
        
        
        public boolean isPlaying() {
            return audioPlayer != null && !audioPlayer.isDone();
        }
    }
    
    public static void main(String[] args) {
    	AudioPlayerDemo audioPlayerGUI = new AudioPlayerDemo();
      audioPlayerGUI.setVisible(true);
    }
}

