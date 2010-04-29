package napplettest;

import napplet.NApplet;
import napplet.NAppletManager;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class NAppletTest extends PApplet {

	NAppletManager nAppletManager;
	NApplet nap;
	
	public void setup() {
		size(300, 300);
		nAppletManager = new NAppletManager(this);
		nAppletManager.createEmbeddedNApplet("MouseBlock", 25, 25);
		nAppletManager.createEmbeddedNApplet("SwingBlock", 175, 25);
		
		nAppletManager.createWindowedNApplet("SwingBlock", 500, 300);
	}
	
	public void draw() {
		background(100);
	}
	
}
