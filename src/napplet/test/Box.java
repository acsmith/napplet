package napplet.test;

import napplet.Nibblet;

public class Box extends Nibblet {

	public Box(int x, int y, int w, int h) {
		nitX = x;
		nitY = y;
		width = w;
		height = h;		
	}
	
	public void setup() {
		stroke(255);
		textAlign(CENTER, CENTER);
	}
	
	public void draw() {
		if (focused)
			fill(100);
		else
			fill(0);
		rect(0, 0, width, height);
		String s = "" + frameCount + ": " + mouseX + ", " + mouseY;
		fill(255);
		text(s, width/2, height/2);
	}
	
}
