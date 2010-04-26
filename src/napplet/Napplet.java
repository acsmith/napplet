package napplet;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import processing.core.PApplet;
import processing.core.PImage;

@SuppressWarnings( { "serial" })
public class Napplet extends PApplet {

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

	PApplet parentPApplet;
	int nappletX;
	int nappletY;
	boolean embeddedNapplet = false;

	public Napplet() {
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

	// These next two need to be replaced with routines that work for embedded
	// NApplets. Obviously delay() will only be manageable down to a resolution
	// of 1/framerate, and framerate() may just not be possible to do in a
	// non-crappy way.

	public void delay(int napTime) {
		System.err.println("NApplet: delay() disabled.");
	}

	public void frameRate(float newRateTarget) {
		System.err.println("NApplet: frameRate(float newRateTarget) disabled.");
	}

	// Disabled cursor manipulation for embedded NApplets for now. Will probably
	// bring it back at some
	// point, but it'll be tricky to manage it properly for embedded NApplets.

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