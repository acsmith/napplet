package napplettest;

import napplet.NitManager;
import napplet.Nit;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class NAppletTest extends PApplet {

	NitManager nitManager;
	Nit nap;
	
	public void setup() {
		size(300, 300);
		nitManager = new NitManager(this);
		nitManager.createEmbeddedNApplet("MouseBlock", 25, 25);
		nitManager.createEmbeddedNApplet("SwingBlock", 75, 75);
		nitManager.createEmbeddedNApplet("SwingBlock", 175, 25);
		nitManager.createWindowedNApplet("SwingBlock", 500, 300);
	}
	
	public void draw() {
		background(100);
	}
	
}
