
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

public class WavPlayer extends JFrame

{
	private Toolbar toolbar;
    private WavPanel wavPanel;
    private JFileChooser chooser;
    private File audioFile;
	private boolean isLaunch = false;
	private SwingWorker<Boolean, Integer> backgroundThread;
	private boolean canDraw = false;

    public WavPlayer() {
    	super("WavPlayer - SourceDataLine");
    	chooser = new JFileChooser();
    	chooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return null;
			}
			@Override
			public boolean accept(File f) {
				if(f.getName().endsWith(".wav")) return true;
				else return false;
			}
		});  

    	setSize(600, 500);
    	setLayout(new BorderLayout());
        toolbar = new Toolbar();
        wavPanel = new WavPanel();
        wavPanel.setPreferredSize(new Dimension(500, 150));
        
        // Ecoute de la sortie audio pour caler l'index de la wavform
        StdAudio.getDataLine().addLineListener(event -> {
                if (event.getType() == LineEvent.Type.START) {
                    canDraw = true;
                } else if (event.getType() == LineEvent.Type.STOP) {
                    canDraw = false;
                    StdAudio.close();
                }
        });
        
        toolbar.setStringListener(new StringListener() {
            @Override
            public void textEmitted(String text) {
                switch (text) {
                    case "Play":
                    	startPlayer();
                        break;
                    case "Pause":
                    	stopPlayer(false);
                        break;
                    case "Stop":
                    	stopPlayer(true);
                        break;
                    case "File":
                    	setFile();
                    default:
                        break;
                }
            }
        });

        // Adds components(child's) into the Layout
        add(toolbar, BorderLayout.NORTH);
        add(wavPanel, BorderLayout.CENTER);

        // Close the program when close the windows
        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    public void stopPlayer(boolean resetIndex) {
    	isLaunch = false;
    	StdAudio.close(); 
    	if(resetIndex) {
    		wavPanel.resetPanelWithFile(audioFile);
    	}
    }

    public void startPlayer() {
	    if(!isLaunch) {
			StdAudio.openAndStartDataLine();
			if(audioFile == null) {
				setFile();
			}else {
				wavPanel.load(audioFile);
				launch();
				isLaunch = true;
				backgroundThread.execute();
			}
		}
    }
    

    public void launch() {
    	backgroundThread = new SwingWorker<Boolean, Integer>() {
    		int curPlaying = 0;
			@Override
			protected Boolean doInBackground() throws Exception {
        	    while(isLaunch && curPlaying != -1) {
        	    	curPlaying = wavPanel.playAndAdvance();
        	    	Thread.sleep((long)(1000/22050));
        	    	publish(curPlaying);
        	    }
        	    if(curPlaying == -1) {
        	    	stopPlayer(true);
        	    }
        	    isLaunch = false;
				return true;
			}
			
			// Update GUI à la fin du thread avec le status
			protected void done() {
				boolean status;
				try {
					status = get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

			// Update GUI avec les éléments publish
			@Override
			protected void process(List<Integer> chunks) {
				if(canDraw) {
					int mostRecentValue = chunks.get(chunks.size()-1);
	            	wavPanel.setCurrentIndexPauseGUI(mostRecentValue);
	            	wavPanel.repaint();
				}
			}
		};
    }

	protected void setFile() {
		if(isLaunch) {
			stopPlayer(false);
		}
		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION && (chooser.getSelectedFile().getName().contains(".wav"))) {
			audioFile = chooser.getSelectedFile();
			wavPanel.resetPanelWithFile(audioFile);
		}
	}

	public static void main(String[] args) {
		new WavPlayer();
	}
}

