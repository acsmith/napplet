package napplet.test;

import napplet.NApplet;

@SuppressWarnings("serial")
public class Throbber extends NApplet {

	int bw = 100;
	int bh = 100;
	int amp = 50;
	int framePeriod = 30;

	public void setup() {
		size(bw, bh);
	}

	public void draw() {
		background(0);
		System.out.println(width + ", " + height);
//		if (frameCount%10==5) {
			int a = (int) (amp * sin(TWO_PI * frameCount / framePeriod));

			resize(bw + a, bh + a);
//		}		
	}
}
