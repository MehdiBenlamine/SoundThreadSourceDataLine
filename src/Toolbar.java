import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar extends JPanel implements ActionListener{

    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton fileButton;
    private  StringListener textListener;
    public Toolbar() {

        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        fileButton = new JButton("File");
        
        
        playButton.addActionListener(this);
        pauseButton.addActionListener(this);
        stopButton.addActionListener(this);
        fileButton.addActionListener(this);
        
        
        setLayout(new FlowLayout(FlowLayout.CENTER));

        add(playButton);
        add(pauseButton);
        add(stopButton);
        add(fileButton);
    }

    public void setStringListener(StringListener listener) {
        this.textListener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Get the button clicked and cast it
       JButton clicked = (JButton) e.getSource();
       
       if(clicked == playButton) {
           if(textListener != null) {
               textListener.textEmitted("Play");
           }
       }
       else if(clicked == pauseButton) {
           if(textListener != null) {
               textListener.textEmitted("Pause");
           }
       } else if(clicked == stopButton){
           if(textListener != null) {
               textListener.textEmitted("Stop");
           }
       } else if(clicked == fileButton){
           if(textListener != null) {
               textListener.textEmitted("File");
           }
       }
    }
}
