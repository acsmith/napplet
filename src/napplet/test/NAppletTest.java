package napplet.test;

import napplet.NApplet;
import napplet.NAppletManager;
import processing.core.PConstants;

@SuppressWarnings("serial")
public class NAppletTest extends NApplet implements PConstants {
	
	NAppletManager nappletManager;
	
	public void setup() {
		System.out.println("Setup");
		size(300, 400);
		nappletManager = new NAppletManager(this);
		nappletManager.createEmbeddedNApplet("MouseBlock", 25, 25);
		nappletManager.createEmbeddedNApplet("SwingBlock", 75, 75);
		nappletManager.createEmbeddedNApplet("SwingBlock", 175, 25);
		nappletManager.createEmbeddedNApplet("Throbber", 75, 175);
		
		nappletManager.createWindowedNApplet("SwingBlock", 500, 300);
		
		nappletManager.setResizable(true);
	}
	
	public void draw() {
		background(100);
	}
	
}
