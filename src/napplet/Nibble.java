package napplet;

import processing.core.PApplet;

public interface Nibble {

	public abstract int getPositionX();

	public abstract int getPositionY();

	public abstract void setPosition(int x, int y);

	public abstract int getWidth();

	public abstract int getHeight();

	public abstract boolean isEmbedded();

	public abstract PApplet getParentPApplet();
	
	public abstract void setParentPApplet(PApplet parentPApplet);
	
	public abstract void setup();
	
	public abstract void draw();

	/**
	 * Run one frame of the NApplet. At present just calls handleDraw() (which
	 * is not subclassed; we're just using the PApplet draw routines; it works
	 * out nicely.)
	 */
	public abstract void runFrame();

}