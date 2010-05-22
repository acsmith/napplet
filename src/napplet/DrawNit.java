package napplet;

public class DrawNit extends NitBase {

	public int frameCount = 0;
	
	@Override
	public void runFrame() {
		frameCount++;
		this.draw();
	}

}
