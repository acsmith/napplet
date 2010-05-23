package napplet.test;

import napplet.NApplet;

@SuppressWarnings("serial")
public class SwingBlock extends NApplet {

	int fillShade = 0;
	int hideTimer;
	
	int blockSize = 10;
	final int MIN_BLOCK_SIZE = 2;
	final int MAX_BLOCK_SIZE = 30;
	final int BLOCK_SIZE_STEP = 4;
	
	public void setup() {
		size(100, 100);
		nappletCloseable = true;
		nappletTint = 0xffffffff;
	}
	
	public void draw() {

		if (nappletHidden) {
			hideTimer--;
			if (hideTimer <=0) {
				show();
			}
		}
		background(0, 125);
		stroke(255);
		fill(fillShade);
		translate(width/2, height/2);
		rotate(frameCount*processing.core.PConstants.PI/180f);
		translate(width/3, 0);
		rect(-blockSize/2, -blockSize/2, blockSize, blockSize);
	}
	
	public void keyPressed() {
		if (key >= '0' && key <= '9') {
			fillShade = 28*(key - '0');
		}
		
		if (key=='h' || key=='H') {
			hide();
			hideTimer = 30;
		}
	}
	
	public void mouseWheelMoved() {
		blockSize -= (mouseWheel - pmouseWheel)*BLOCK_SIZE_STEP;
		blockSize = max(blockSize, MIN_BLOCK_SIZE);
		blockSize = min(blockSize, MAX_BLOCK_SIZE);
	}
	
}
