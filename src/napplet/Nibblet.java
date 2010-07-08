package napplet;

import java.awt.Frame;

import processing.core.PStyle;

/**
 * Nit implementation that draws directly on its parent's display space.
 * 
 * @author acsmith
 * 
 */
public class Nibblet extends NitBase {

	public PStyle pStyle = new PStyle();
	public boolean persistentStyle = true;
	public boolean resetMatrix = true;
	public int frameCount = 0;

	public Nibblet() {
		super();
	}

	public void setNAppletManager(NAppletManager nappletManager) {
		super.setNAppletManager(nappletManager);
		this.g = parentPApplet.g;
	}

	@Override
	public void runFrame() {
	
		boolean internalPersistentStyle = persistentStyle;
		boolean internalResetMatrix = resetMatrix;
	
		if (frameCount == 0)
			g.getStyle(pStyle);
	
		if (internalPersistentStyle) {
			g.pushStyle();
			g.style(pStyle);
		}
		if (internalResetMatrix) {
			g.pushMatrix();
			g.resetMatrix();
			g.translate(getPositionX(), getPositionY());
		}
		if (frameCount == 0) {
			this.preSetup();
			this.setup();
			this.postSetup();
		} else {
			this.preDraw();
			this.draw();
			this.postDraw();
		}
		if (internalResetMatrix) {
			g.popMatrix();
		}
		if (internalPersistentStyle) {
			g.getStyle(pStyle);
			g.popStyle();
		}
	
		frameCount++;
	
	}

	public void size(int w, int h) {
		width = w;
		height = h;
	}

	public void position(int x, int y) {
		nitX = x;
		nitY = y;
	}

	public void preSetup() {
	}

	public void postSetup() {
	}

	public void preDraw() {
	}

	public void postDraw() {
	}

	@Override
	public Frame getFrame() {
		return null;
	}

}