package napplet.test;

import napplet.NAppletManager;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class NitTest1 extends PApplet {

	NAppletManager nappletManager;
	
	public void setup() {
		size(300, 300, P2D);
		nappletManager = new NAppletManager(this);
		
		nappletManager.addNit(new Box(100, 100, 100, 100));
		nappletManager.createWindowedNApplet("SwingBlock", 500, 300);
	}
	
	public void draw() {
		background(0);
		stroke(255, 100, 100);
		rect(width/6, 2*height/5, 2*width/3, height/5);
		
	}
	
}
