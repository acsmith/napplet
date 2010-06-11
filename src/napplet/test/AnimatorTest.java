package napplet.test;

import processing.core.PApplet;
import napplet.NApplet;
import napplet.NAppletManager;

@SuppressWarnings("serial")
public class AnimatorTest extends PApplet {

	NAppletManager nappletManager; 
	
	public void setup() {
		size(300, 300, P2D);
		nappletManager = new NAppletManager(this);

		nappletManager.createWindowedNApplet("napplet.test.Animator", 500, 300);

		NApplet embedAnim = nappletManager.createEmbeddedNApplet("napplet.test.Animator", 50, 50);
		embedAnim.nappletTint = 0xffffffff;
	}

	public void draw() {
		background(255);
		stroke(0);
		fill(125);
		strokeWeight(6);
		rect(width/3, height/12, width/3, 5*height/6);
	}
}
