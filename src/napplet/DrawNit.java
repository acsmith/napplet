package napplet;

public class DrawNit extends NitBase {

	@Override
	public void runFrame() {
		parentPApplet.pushMatrix();
		parentPApplet.translate(nitX, nitY);
		this.draw();
		parentPApplet.popMatrix();

	}

}
