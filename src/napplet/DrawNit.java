package napplet;

/**
 * Nit implementation that draws directly on its parent's display space.
 * 
 * @author acsmith
 * 
 */
public class DrawNit extends NitBase {

	public int frameCount = 0;

	@Override
	public void runFrame() {
		frameCount++;
		this.draw();
	}

}
