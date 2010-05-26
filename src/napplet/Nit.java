package napplet;

import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;

/**
 * Interface for objects managed by NAppletManager. Applies to NApplets
 * obviously, but can also be used for lighter objects, even those without their
 * own display space (e.g., a "NApplet lite" type object that draws in its
 * parent's space.)
 * 
 * @author acsmith
 * 
 */
public interface Nit {

	/**
	 * Returns the x coordinate of the Nit's upper left corner in its parent's
	 * display space.
	 * 
	 * @return x-coordinate
	 */
	public abstract int getPositionX();

	/**
	 * Return the y coordinate of the Nit's upper left corner in its parent's
	 * display space.
	 * 
	 * @return y-coordinate
	 */
	public abstract int getPositionY();

	/**
	 * Set the Nit's position in its parent's display space.
	 * 
	 * @param x
	 *            horizontal position of the Nit's upper left corner.
	 * @param y
	 *            vertical position of the Nit's upper left corner.
	 */
	public abstract void setPosition(int x, int y);

	/**
	 * Return the Nit's width in pixels.
	 * 
	 * @return width of the Nit in pixels
	 */
	public abstract int getWidth();

	/**
	 * Return the Nit's height in pixels.
	 * 
	 * @return height of the Nit in pixels
	 */
	public abstract int getHeight();

	/**
	 * Tells whether the Nit is embedded in another Nit's display window, or has
	 * its own window.
	 * 
	 * @return true for an embedded Nit, false for a Nit in its own window
	 *         (including a NApplet running in standalone mode, i.e., as a
	 *         PApplet.)
	 */
	public abstract boolean isEmbedded();

	/**
	 * Get the NAppletManager object responsible for handling this Nit.
	 * 
	 * @return managing NAppletManager
	 */
	public abstract NAppletManager getNAppletManager();

	/**
	 * Set the NAppletManager responsible for this Nit.
	 * 
	 * @param nappletManager
	 */
	public abstract void setNAppletManager(NAppletManager nappletManager);

	/**
	 * Set up Nit-specific variables, etc. Similar to PApplet.setup().
	 */
	public abstract void setup();

	/**
	 * Draw the Nit. Similar to PApplet.draw();
	 */
	public abstract void draw();

	/**
	 * Filter to determine whether to pass an input event (generally mouse or
	 * keyboard) to the Nit.
	 * 
	 * @param x
	 *            Mouse x position (in the Nit's coordinates.)
	 * @param y
	 *            Mouse y position (in the Nit's coordinates.)
	 * @return true if the Nit wants to claim the event; otherwise gets passed
	 *         through to any other Nit(s) that might be eligible.
	 */
	public abstract boolean inputHit(int x, int y);

	/**
	 * Pass any java.awt.event.InputEvent objects to the Nit. (Generally mouse
	 * or keyboard.)
	 * 
	 * @param event
	 */
	public abstract void passEvent(InputEvent event);
	
	/**
	 * Inform the Nit that it's gained focus.
	 * 
	 * @param gainFocus
	 */
	public abstract void focusGained(FocusEvent gainFocus);

	/**
	 * Inform the Nit that it's lost focus.
	 * 
	 * @param loseFocus
	 */
	public abstract void focusLost(FocusEvent loseFocus);

	/**
	 * Run and draw a single graphics frame. Usually calls the Nit's draw()
	 * method and in some cases may do other things as well.
	 */
	public abstract void runFrame();

}