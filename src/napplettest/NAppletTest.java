package napplettest;

import napplet.NAppletManager;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class NAppletTest extends PApplet {

	NAppletManager nitManager;
	
	public void setup() {
		size(300, 300);
		nitManager = new NAppletManager(this);
		nitManager.createEmbeddedNApplet("MouseBlock", 25, 25);
		nitManager.createEmbeddedNApplet("SwingBlock", 75, 75);
		nitManager.createEmbeddedNApplet("SwingBlock", 175, 25);
		nitManager.createWindowedNApplet("SwingBlock", 500, 300);
	}
	
	public void draw() {
		background(100);
	}
	
}
