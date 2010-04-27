package napplet.test;

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
		nAppletManager.createNApplet("MouseBlock", 25, 25);
		nAppletManager.createNApplet("MouseBlock", 175, 175);
		nAppletManager.createNApplet("SwingBlock", 25, 175);
		nAppletManager.createNApplet("SwingBlock", 175, 25);
	}
	
	public void draw() {
		background(100);
	}
	
}
