package napplet;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import processing.core.PApplet;
import processing.core.PImage;

@SuppressWarnings({ "serial", "unused" })
public class Napplet extends PApplet {

	volatile boolean resizeRequest;
	volatile int resizeWidth;
	volatile int resizeHeight;

	/**
	 * Time in milliseconds when the applet was started.
	 * <P>
	 * Used by the millis() function.
	 */
	long millisOffset;

	//Thread thread;

	// New members for Napplet

	PApplet parentPApplet;
	int nappletX;
	int nappletY;
	boolean embeddedNapplet = false;

	public Napplet() {
	}

	/**
	 * Replaces call to init() for sub-sketch Napplets (I hope.)
	 */
	public void nappletInit(PApplet pap, int x, int y, int w, int h,
			String skPath) {

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

		sketchPath = skPath;

		g = makeGraphics(w, h, getSketchRenderer(), null, true);

		width = g.width;
		height = g.height;
		nappletX = x;
		nappletY = y;
		embeddedNapplet = true;
	}

	public void passMouseEvent(MouseEvent event) {
		this.enqueueMouseEvent(event);
	}

	public void passKeyEvent(KeyEvent event) {
		this.enqueueKeyEvent(event);
	}
	
	public void setup() {
	}

	public void draw() {
		finished = true;
	}

	protected void paint() {
		if (embeddedNapplet) {
			loadPixels();
			parentPApplet.image(this.g, nappletX, nappletY);
		} else
			super.paint();
	}

	// ////////////////////////////////////////////////////////////

	public void focusGained() {
	}

	public void focusGained(FocusEvent e) {
		focused = true;
		focusGained();
	}

	public void focusLost() {
	}

	public void focusLost(FocusEvent e) {
		focused = false;
		focusLost();
	}

	// ////////////////////////////////////////////////////////////

	// controlling time (playing god)

	public void delay(int napTime) {
		System.err.println("Napplet: delay() disabled.");
	}

	public void frameRate(float newRateTarget) {
		System.err.println("Napplet: frameRate(float newRateTarget) disabled.");
	}

	// ////////////////////////////////////////////////////////////

	// CURSOR

	public void cursor(int cursorType) {
		System.err.println("Napplet: Cursor manipulation disabled for now.");
	}

	public void cursor(PImage image) {
		System.err.println("Napplet: Cursor manipulation disabled for now.");
	}

	public void cursor(PImage image, int hotspotX, int hotspotY) {
		System.err.println("Napplet: Cursor manipulation disabled for now.");
	}

	public void cursor() {
		System.err.println("Napplet: Cursor manipulation disabled for now.");
	}

	public void noCursor() {
		System.err.println("Napplet: Cursor manipulation disabled for now.");
	}

	// ////////////////////////////////////////////////////////////

	// MAIN

	/**
	 * Set this sketch to communicate its state back to the PDE.
	 * <p/>
	 * This uses the stderr stream to write positions of the window (so that it
	 * will be saved by the PDE for the next run) and notify on quit. See more
	 * notes in the Worker class.
	 */
	public void setupExternalMessages() {

		frame.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				Point where = ((Frame) e.getSource()).getLocation();
				System.err.println(PApplet.EXTERNAL_MOVE + " " + where.x + " "
						+ where.y);
				System.err.flush(); // doesn't seem to help or hurt
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// System.err.println(PApplet.EXTERNAL_QUIT);
				// System.err.flush(); // important
				// System.exit(0);
				exit(); // don't quit, need to just shut everything down (0133)
			}
		});
	}

	/**
	 * Set up a listener that will fire proper component resize events in cases
	 * where frame.setResizable(true) is called.
	 */
	public void setupFrameResizeListener() {
		frame.addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				// Ignore bad resize events fired during setup to fix
				// http://dev.processing.org/bugs/show_bug.cgi?id=341
				// This should also fix the blank screen on Linux bug
				// http://dev.processing.org/bugs/show_bug.cgi?id=282
				if (frame.isResizable()) {
					// might be multiple resize calls before visible (i.e. first
					// when pack() is called, then when it's resized for use).
					// ignore them because it's not the user resizing things.
					Frame farm = (Frame) e.getComponent();
					if (farm.isVisible()) {
						Insets insets = farm.getInsets();
						Dimension windowSize = farm.getSize();
						int usableW = windowSize.width - insets.left
								- insets.right;
						int usableH = windowSize.height - insets.top
								- insets.bottom;

						// the ComponentListener in PApplet will handle calling
						// size()
						setBounds(insets.left, insets.top, usableW, usableH);
					}
				}
			}
		});
	}

}