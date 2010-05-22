package napplettest;

import napplet.DrawNit;

public class Box extends DrawNit {

	public Box(int x, int y, int w, int h) {
		nitX = x;
		nitY = y;
		width = w;
		height = h;
	}
	
	public void draw() {
		parentPApplet.stroke(255);
		parentPApplet.fill(150);
		parentPApplet.rect(0, 0, width, height);
	}
	
}
