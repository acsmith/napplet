package napplet;

@SuppressWarnings("serial")
public class Swingblock extends Napplet {

	public void setup() {
		size(100, 100);
	}
	
	public void draw() {
		background(0);
		stroke(255);
		fill(100);
		translate(width/2, height/2);
		rotate(frameCount*processing.core.PConstants.PI/180f);
		translate(width/3, 0);
		rect(-5, -5, 10, 10);
	}
	
}
