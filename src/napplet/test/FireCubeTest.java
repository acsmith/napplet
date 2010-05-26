package napplet.test;

import napplet.NApplet;
import napplet.NAppletManager;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class FireCubeTest extends PApplet {

	NAppletManager nappletManager; 
	
	public void setup() {
		size(300,300);
		nappletManager = new NAppletManager(this);
		NApplet embedCube = nappletManager.createEmbeddedNApplet("napplet.test.FireCube", 50, 50);

		nappletManager.createWindowedNApplet("napplet.test.FireCube", 500, 300);
		
		embedCube.nappletTint = 0xc0ffffff;
	}

	public void draw() {
		background(255);
		stroke(0);
		fill(125);
		strokeWeight(6);
		rect(width/3, height/12, width/3, 5*height/6);
	}
}
