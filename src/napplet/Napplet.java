package napplet;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import processing.core.PApplet;
import processing.core.PImage;

@SuppressWarnings( { "serial" })
public class NApplet extends PApplet {

	/**
	 * Time in milliseconds when the applet was started. We need to have our own
	 * number for this since PApplet's millisOffset is private.
	 */
	long millisOffset;

	/**
	 * Overrides PApplet's millis() routine.
	 */
	public int millis() {
		return (int) (parentPApplet.millis() - millisOffset);
	}

	// New members for Napplet

	/**
	 * The PApplet (or NApplet) that this NApplet is displayed on, if any.
	 */
	PApplet parentPApplet = null;

	/**
	 * The x-position (in screen coordinates) of this NApplet's display space in
	 * its parent's display.
	 */
	int nappletX;

	/**
	 * The y-position(in screen coordinates) of this NApplet's display space in
	 * its parent's display.
	 */
	int nappletY;

	/**
	 * True if this NApplet is displaying in another PApplet's (or NApplet's)
	 * display space, false if it's a standalone applet.
	 */
	boolean embeddedNapplet = false;

	/**
	 * Do-nothing constructor.  Use nappletInit() to initialize a NApplet.
	 */
	public NApplet() {
	}

	/**
	 * Used to initialize an embedded NApplet. Replaces the call to init().
	 * 
	 * @param pap
	 *            Parent PApplet (or NApplet)
	 * @param x
	 *            x-coordinate of top-left corner of this NApplet's area within
	 *            the parent's graphic space.
	 * @param y
	 *            y-coordinate of top-left corner of this NApplet's area within
	 *            the parent's graphic space.
	 * @param w
	 *            Width of this NApplet's graphic space.
	 * @param h
	 *            Height of this NApplet's graphic space.
	 * @param sketchPath
	 *            Path for this NApplet's home folder.
	 */
	public void nappletInit(PApplet pap, int x, int y, int w, int h,
			String sketchPath) {

		parentPApplet = pap;

		millisOffset = parentPApplet.millis();

		finished = false;
		looping = true;
		redraw = true;
		firstMouse = true;

		sizeMethods = new RegisteredMethods();
		preMethods = new RegisteredMethods();
		drawMethods = new RegisteredMethods();
		postMethods = new RegisteredMethods();
		mouseEventMethods = new RegisteredMethods();
		keyEventMethods = new RegisteredMethods();
		disposeMethods = new RegisteredMethods();

		online = parentPApplet.online;

		this.sketchPath = sketchPath;

		g = makeGraphics(w, h, getSketchRenderer(), null, true);

		width = g.width;
		height = g.height;
		nappletX = x;
		nappletY = y;
		embeddedNapplet = true;
	}

	/**
	 * Accessor for queueing mouse events. Used by the NAppletManager.
	 * 
	 * @param event
	 *            Mouse event. This needs to be translated to the NApplet's
	 *            local screen coordinates.
	 */
	public void passMouseEvent(MouseEvent event) {
		this.enqueueMouseEvent(event);
	}

	/**
	 * Accessor for queueing keyboard events. Used by the NAppletManager.
	 * 
	 * @param event
	 *            Keyboard event.
	 */
	public void passKeyEvent(KeyEvent event) {
		this.enqueueKeyEvent(event);
	}

	/**
	 * Override PApplet.paint(). If the NApplet is embedded, uses the
	 * PApplet.image() method to paint the NApplet's pixels into the parent's
	 * display. Otherwise, just falls through to PApplet.paint().
	 */
	protected void paint() {
		if (embeddedNapplet) {
			loadPixels();
			parentPApplet.image(this.g, nappletX, nappletY);
		} else
			super.paint();
	}

	/**
	 * Overrides PApplet.delay(). For now, this just means delay() is disabled
	 * for embedded NApplets. For standalone NApplets, this just passes through
	 * to PApplet.delay().
	 */
	public void delay(int napTime) {
		if (embeddedNapplet)
			System.err.println("NApplet: delay() disabled.");
		else
			super.delay(napTime);
	}

	/**
	 * Override for PApplet.frameRate(). Just disables frame rate setting for
	 * embedded NApplets, and passes through to PApplet.frameRate() for
	 * standalone NApplets.
	 */
	public void frameRate(float newRateTarget) {
		if (embeddedNapplet)
			System.err
					.println("NApplet: frameRate(float newRateTarget) disabled.");
		else
			super.frameRate(newRateTarget);
	}

	// Disabled cursor manipulation for embedded NApplets for now. Will probably
	// bring it back at some point, but it'll be tricky to manage it properly
	// for embedded NApplets.

	public void cursor(int cursorType) {
		if (embeddedNapplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.cursor(cursorType);
	}

	public void cursor(PImage image) {
		if (embeddedNapplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.cursor(image);
	}

	public void cursor(PImage image, int hotspotX, int hotspotY) {
		if (embeddedNapplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.cursor(image, hotspotX, hotspotY);
	}

	public void cursor() {
		if (embeddedNapplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.cursor();
	}

	public void noCursor() {
		if (embeddedNapplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.noCursor();
	}

}