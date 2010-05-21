package napplettest;

import napplet.NApplet;


@SuppressWarnings("serial")
public class MouseBlock extends NApplet {

	int blockSize = 10;
	final int MIN_BLOCK_SIZE = 2;
	final int MAX_BLOCK_SIZE = 30;
	final int BLOCK_SIZE_STEP = 4;
	
	public void setup() {
		size(100, 100);
	}

	int blockX = width/2;
	int blockY = height/2;
	public void draw() {
		if (mousePressed) {
			blockX = mouseX;
			blockY = mouseY;
		}
		background(0);
		stroke(255);
		fill(100);
		translate(blockX, blockY);
		rotate(frameCount*processing.core.PConstants.PI/180f);
		rect(-blockSize/2, -blockSize/2, blockSize, blockSize);		
	}

	public void mouseWheelMoved() {
		blockSize -= (mouseWheel - pmouseWheel)*BLOCK_SIZE_STEP;
		blockSize = max(blockSize, MIN_BLOCK_SIZE);
		blockSize = min(blockSize, MAX_BLOCK_SIZE);
	}
}
