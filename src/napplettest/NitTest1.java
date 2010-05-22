package napplettest;

import napplet.NAppletManager;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class NitTest1 extends PApplet {

	NAppletManager nappletManager;
	
	public void setup() {
		size(300, 300);
		nappletManager = new NAppletManager(this);
		
		nappletManager.addNit(new Box(100, 100, 100, 100));
//		
//		nappletManager.createEmbeddedNApplet("MouseBlock", 25, 25);
//		nappletManager.createEmbeddedNApplet("SwingBlock", 75, 75);
//		nappletManager.createEmbeddedNApplet("SwingBlock", 175, 25);
//		nappletManager.createWindowedNApplet("SwingBlock", 500, 300);
	}
	
	public void draw() {
		background(0);
		
	}
	
}
