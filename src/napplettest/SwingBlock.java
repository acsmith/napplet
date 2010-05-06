package napplettest;

import napplet.NApplet;

@SuppressWarnings("serial")
public class SwingBlock extends NApplet {

	int fillShade = 0;
	int hideTimer;
	
	public void setup() {
		size(100, 100);
		nappletCloseable = true;
	}
	
	public void draw() {

		if (nappletHidden) {
			hideTimer--;
			if (hideTimer <=0) {
				show();
			}
		}
		background(0, 0, 0, 125);
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
		
		if (key=='h' || key=='H') {
			hide();
			hideTimer = 30;
		}
	}
	
}
