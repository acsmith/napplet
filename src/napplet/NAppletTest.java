package napplet;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class NAppletTest extends PApplet {

	Napplet nap;
	
	public void setup() {
		size(200, 200);
		nap = new Swingblock();
		nap.nappletInit(this, 50, 50, 100, 100, sketchPath);
	}
	
	public void draw() {
		background(100);
		nap.handleDraw();
	}
	
}
