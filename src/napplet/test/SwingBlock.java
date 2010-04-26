package napplet.test;

import napplet.NApplet;

@SuppressWarnings("serial")
public class SwingBlock extends NApplet {

	int fillShade = 0;
	
	public void setup() {
		size(100, 100);
	}
	
	public void draw() {
		background(0);
		stroke(255);
		fill(fillShade);
		translate(width/2, height/2);
		rotate(frameCount*processing.core.PConstants.PI/180f);
		translate(width/3, 0);
		rect(-5, -5, 10, 10);
	}
	
	public void keyPressed() {
		if (key >= '0' && key <= '9') {
			fillShade = 28*(key - '0');
		}
	}
	
}
