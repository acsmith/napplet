package napplet;

import processing.core.PStyle;

/**
 * Nit implementation that draws directly on its parent's display space.
 * 
 * @author acsmith
 * 
 */
public class DrawNit extends NitBase {

	public PStyle pStyle = new PStyle();

	public boolean persistentStyle = true;
	public boolean resetMatrix = true;

	public int frameCount = 0;

	@Override
	public void runFrame() {

		frameCount++;

		boolean internalPersistentStyle = persistentStyle;
		boolean internalResetMatrix = resetMatrix;

		if (internalPersistentStyle) {
			parentPApplet.g.pushStyle();
			parentPApplet.g.style(pStyle);
		}
		if (internalResetMatrix) {
			parentPApplet.g.pushMatrix();
			parentPApplet.g.resetMatrix();
			parentPApplet.g.translate(getPositionX(), getPositionY());
		}
		this.draw();
		if (internalResetMatrix) {
			parentPApplet.g.popMatrix();
		}
		if (internalPersistentStyle) {
			parentPApplet.g.getStyle(pStyle);
			parentPApplet.g.popStyle();
		}
	}

}
