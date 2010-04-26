package napplet;


@SuppressWarnings("serial")
public class MouseBlock extends Napplet {

	public void setup() {
		size(100, 100);
	}

	int blockX = width/2;
	int blockY = height/2;
	public void draw() {
		if (mousePressed) {
			blockX = mouseX;
			blockY = mouseY;
		}
		background(0);
		stroke(255);
		fill(100);
		translate(blockX, blockY);
		rotate(frameCount*processing.core.PConstants.PI/180f);
		rect(-5, -5, 10, 10);
	}
	
}
