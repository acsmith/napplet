package napplettest;

import napplet.NibbleManager;
import napplet.Nibble;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class NAppletTest extends PApplet {

	NibbleManager nibbleManager;
	Nibble nap;
	
	public void setup() {
		size(300, 300);
		nibbleManager = new NibbleManager(this);
		nibbleManager.createEmbeddedNApplet("MouseBlock", 25, 25);
		nibbleManager.createEmbeddedNApplet("SwingBlock", 75, 75);
		nibbleManager.createEmbeddedNApplet("SwingBlock", 175, 25);
		
		nibbleManager.createWindowedNApplet("SwingBlock", 500, 300);
	}
	
	public void draw() {
		background(100);
	}
	
}
