import java.awt.*;
import java.io.File;

import javax.swing.*;

public class WavPanel extends JPanel 
{

	// Holds the currently loaded audio data
	private double [] audio = null;
	
	// Present playback position, -1 if not currently playing
	private int currentIndex = -1;
	
	// Present GUI playback position, -1 if not currently playing
	public int curIndexPosGUI = 0;
	
	// Attempts to load the audio in the given filename.
	// Returns true on success, false otherwise.
	public boolean load(File filename)
	{
		audio = StdAudio.read(filename);
		if(audio == null || audio.length == 0) {
			audio = null;
			return false;
		}else {
			if(currentIndex == -1) {
				currentIndex = 0;
			}
			repaint();
			return true;
		}
	}

	// Return the number of samples in the currently loaded audio.
	// Returns 0 if no audio loaded.
	public int getNumSamples()
	{
		return (audio == null ? 0 : audio.length);
	}

	// Get the index of the next audio sample that will be played.
	// Returns -1 if playback isn't active.
	public int getCurrentIndex()
	{	
		return currentIndex;
	}

	// Sets the index of the current audio sample.
	// Client may set to -1 when playback is not active (no red line drawn).
	// Client may set to 0 when playback is about to start.
	public void setCurrentIndex(int i)
	{
		currentIndex = i;
	}

	// Play a single audio sample based on the current index.
	// Advance the index one position.
	// Returns the panel x-coordinate of the played sample.
	// Returns -1 if playback failed (no audio or index out of range).
	public int playAndAdvance()
	{
		if(audio == null || currentIndex >= audio.length)
		{
			return -1;
		}else {
			StdAudio.play(audio[currentIndex++]);
			return (currentIndex-1) * this.getWidth() / audio.length;
		}
	}

	public void setCurrentIndexPauseGUI(int index) {
		curIndexPosGUI = index;
	}

	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
	    Graphics2D g2d = (Graphics2D) g;
	    if(audio != null) {
			for (int i = 0; i < audio.length; i++) {
		    	int x = i * getWidth() / audio.length;
	    		double toDrawVal = Math.abs(audio[i]);
		    	double lengthToDraw = getHeight()/2 * toDrawVal;
		    	int yMin = (int)(getHeight() - lengthToDraw) / 2;
		    	int yMax = (int)(yMin + lengthToDraw);
		    	g.setColor(Color.BLUE);
		    	g2d.drawLine(x, yMin, x, yMax);
		    }
    		g.setColor(Color.RED);
    		g2d.drawLine(curIndexPosGUI, 0, curIndexPosGUI, getHeight());
		}
	}

	public void resetPanelWithFile(File fileName) {
    	setCurrentIndex(-1);
    	setCurrentIndexPauseGUI(0);
    	load(fileName);
    	repaint();
	}

}

