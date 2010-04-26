package napplet;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class NAppletTest extends PApplet {

	NAppletManager nAppletManager;
	NApplet nap;
	
	public void setup() {
		size(300, 300);
		nAppletManager = new NAppletManager(this);
		nap = new MouseBlock();
		nap.nappletInit(this, 25, 25, 100, 100, sketchPath);
		nAppletManager.addNapplet(nap);
		nap = new MouseBlock();
		nap.nappletInit(this, 175, 175, 100, 100, sketchPath);
		nAppletManager.addNapplet(nap);
		nap = new SwingBlock();
		nap.nappletInit(this, 25, 175, 100, 100, sketchPath);
		nAppletManager.addNapplet(nap);
		nap = new SwingBlock();
		nap.nappletInit(this, 175, 25, 100, 100, sketchPath);
		nAppletManager.addNapplet(nap);
	}
	
	public void draw() {
		background(100);
	}
	
}
