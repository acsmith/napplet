package napplet.test;

import napplet.NAppletManager;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class FireCubeTest extends PApplet {

	NAppletManager nappletManager; 
	
	public void setup() {
		size(300,300);
		nappletManager = new NAppletManager(this);
		
		nappletManager.createEmbeddedNApplet("napplet.test.FireCube", 50, 50);
	}

	public void draw() {
		background(255);
		stroke(0);
		rect(width/3, width/12, width/3, 5*width/6);
	}
}
