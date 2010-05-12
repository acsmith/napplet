package napplet;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Subclass of processing.core.PApplet (Processing main sketch class.)
 * 
 * @author acsmith
 * 
 */
@SuppressWarnings( { "serial" })
public class NApplet extends PApplet implements Nit, MouseWheelListener {

	public static final String VERSION = "0.1.0";

	public String version() {
		return VERSION;
	}

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
	public PApplet parentPApplet = null;

	/**
	 * The manager for this NApplet. Remains null if this NApplet is being run
	 * on its own, otherwise the manager will set it.
	 */
	public NitManager nitManager = null;

	/**
	 * The x-position of this NApplet's display space in its parent's display,
	 * or the x-position of the NApplet's window on the screen.
	 */
	public int nappletX;

	/**
	 * The y-position of this NApplet's display space in its parent's display,
	 * or the y-position of the NApplet's window on the screen.
	 */
	public int nappletY;

	/**
	 * True if this NApplet is displaying in another PApplet's (or NApplet's)
	 * display space, false if it's a stand-alone applet or in its own window.
	 */
	public boolean embeddedNApplet = false;

	/**
	 * True if this NApplet is in its own window as a sub-sketch of another
	 * sketch, but not running by itself.
	 */
	public boolean windowedNApplet = false;

	/**
	 * True if this NApplet has been hidden with the hide() method. Set to false
	 * after un-hidden with show().
	 */
	public boolean nappletHidden = false;

	/**
	 * True if the user can close the NApplet through the normal window-closing
	 * controls (whatever those may be).
	 */
	public boolean nappletCloseable = false;

	/**
	 * Tint used for pasting this NApplet into its parent's display space.
	 * Mainly useful for setting the alpha channel to get translucency. Should
	 * be settable with Processing's "color datatype", e.g., nappletTint =
	 * color(255, 127) or whatever.
	 */
	public int nappletTint = 0xffffffff;

	/**
	 * Current position of the mouse wheel. Starts at zero, increases by one for
	 * each "click" the wheel is rotated towards the user (i.e., "down"),
	 * decreases for each click rotated away from the user ("up").
	 */
	public int mouseWheel = 0;

	/**
	 * Previous position of the mouse wheel (before the most recent movement of
	 * the wheel.)
	 */
	public int pmouseWheel = 0;

	/**
	 * Do-nothing constructor. Use initEmbeddedNApplet() or
	 * initWindowedNApplet() to initialize a NApplet.
	 */
	public NApplet() {
	}

	/**
	 * Does the main grunt-work of NApplet initialization. Mostly it's just
	 * transplanted stuff from PApplet.init(); the intent is to make Pinocchio
	 * as much like a real boy as possible.
	 * 
	 * @param pap
	 *            Parent PApplet (or NApplet)
	 * @param x
	 *            x-coordinate of the NApplet
	 * @param y
	 *            y-coordinate of the NApplet
	 * @param sketchPath
	 *            home folder for the NApplet (potentially this will be useful
	 *            later on for napplet-izing an existing processing sketch (need
	 *            to write a tool for this).
	 */
	protected void initNApplet(PApplet pap, int x, int y, String sketchPath) {
		parentPApplet = pap;

		// Have to do this because PApplet.millisOffset is private.
		millisOffset = parentPApplet.millis();

		// Everything else is basically just transplanted initialization stuff
		// from PApplet.init().
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
		nappletX = x;
		nappletY = y;
		{
			this.defaultSize = true;
			int w = getSketchWidth();
			int h = getSketchHeight();
			g = makeGraphics(w, h, getSketchRenderer(), null, true);
			setSize(w, h);
			setPreferredSize(new Dimension(w, h));
		}
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
	 * @param sketchPath
	 *            Path for this NApplet's home folder.
	 */
	public void initEmbeddedNApplet(PApplet pap, int x, int y, String sketchPath) {

		initNApplet(pap, x, y, sketchPath);

		// Use the parent's size as the "screen size" for this applet.
		screenWidth = parentPApplet.width;
		screenHeight = parentPApplet.height;

		embeddedNApplet = true;

		setup();
	}

	/**
	 * Used to initialize a windowed NApplet. Replaces the call to init().
	 * 
	 * @param pap
	 *            Parent PApplet (or NApplet)
	 * @param x
	 *            x-coordinate of top-left corner of this NApplet's window.
	 * @param y
	 *            y-coordinate of top-left corner of this NApplet's window.
	 * @param sketchPath
	 *            Path for this NApplet's home folder.
	 */
	public void initWindowedNApplet(PApplet pap, int x, int y, String sketchPath) {

		initNApplet(pap, x, y, sketchPath);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screenWidth = screen.width;
		screenHeight = screen.height;

		windowedNApplet = true;

		addListeners();
		setup();

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
	 */
	public void initEmbeddedNApplet(PApplet pap, int x, int y) {
		initEmbeddedNApplet(pap, x, y, pap.sketchPath);
	}

	/**
	 * Initializes an embedded NApplet with its top-left corner at (0,0) in the
	 * parent's display.
	 * 
	 * @param pap
	 *            ParentPApplet (or NApplet)
	 */
	public void initEmbeddedNApplet(PApplet pap) {
		initEmbeddedNApplet(pap, 0, 0, pap.sketchPath);
	}

	public void addListeners() {
		super.addListeners();
		addMouseWheelListener(this);
	}

	/**
	 * Allow for user closing of windowed NApplets.
	 */
	public void setupNAppletMessages() {
		if (windowedNApplet)
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					userWindowClose();
				}
			});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nibble#getPositionX()
	 */
	public int getPositionX() {
		return nappletX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nibble#getPositionY()
	 */
	public int getPositionY() {
		return nappletY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nibble#setPosition(int, int)
	 */
	public void setPosition(int x, int y) {
		nappletX = x;
		nappletY = y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nibble#getWidth()
	 */
	public int getWidth() {
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nibble#getHeight()
	 */
	public int getHeight() {
		return height;
	}

	public boolean isHidden() {
		return nappletHidden;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nibble#isEmbedded()
	 */
	public boolean isEmbedded() {
		return embeddedNApplet;
	}

	public void setParentPApplet(PApplet pap) {
		parentPApplet = pap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nibble#getParentPApplet()
	 */
	public PApplet getParentPApplet() {
		return parentPApplet;
	}

	public void setManager(NitManager nappletManager) {
		this.nitManager = nappletManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nibble#runFrame()
	 */
	public void runFrame() {

		handleDraw();
	}

	/**
	 * Hides the NApplet.
	 */
	public void hide() {
		if (frame != null) {
			frame.setVisible(false);
		}
		nappletHidden = true;
	}

	/**
	 * Unhides the NApplet.
	 */
	public void show() {
		if (frame != null) {
			frame.setVisible(true);
		}
		nappletHidden = false;
	}

	/**
	 * Called when a window-closing event is received (presumably the user
	 * clicking the close widget.)
	 */
	public void userWindowClose() {
		if (nappletCloseable)
			exit();
	}

	/**
	 * Override for PApplet.exit(). Handles things for a windowed or embedded
	 * NApplet, or falls through to PApplet.exit() for a standalone.
	 */
	public void exit() {
		if (windowedNApplet)
			frame.dispose();
		if (embeddedNApplet || windowedNApplet)
			nitManager.killNit(this);
		else
			super.exit();
	}

	/**
	 * Override for PApplet.size(). Falls through for standalone or windowed
	 * NApplets.
	 * 
	 * @param iwidth
	 *            Desired width
	 * @param iheight
	 *            Desired height
	 * @param irenderer
	 *            Desired renderer
	 * @param ipath
	 *            Desired um... path? I forget what this does; it's passed
	 *            through to the same argument for PApplet.makeGraphics().
	 */
	public void size(final int iwidth, final int iheight, String irenderer,
			String ipath) {
		if (embeddedNApplet) {
			if (g == null) {
				g = makeGraphics(iwidth, iheight, irenderer, ipath, false);
				width = iwidth;
				height = iheight;
			} else {
				String currentRenderer = g.getClass().getName();
				if (currentRenderer.equals(irenderer))
					resizeRenderer(iwidth, iheight);
				else {
					g = makeGraphics(iwidth, iheight, irenderer, ipath, true);
					width = iwidth;
					height = iheight;
				}
			}
		} else {
			setSize(iwidth, iheight);
			setPreferredSize(new Dimension(iwidth, iheight));

			super.size(iwidth, iheight, irenderer, ipath);
		}
	}

	/**
	 * Accessor for queueing mouse events. Used by the NAppletManager since
	 * PApplet.enqueueMouseEvent() is protected.
	 * 
	 * @param event
	 *            Mouse event. This needs to be translated to the NApplet's
	 *            local screen coordinates.
	 */
	public void passMouseEvent(MouseEvent event) {
		this.enqueueMouseEvent(event);
	}

	public void passMouseWheelEvent(MouseWheelEvent event) {
		this.mouseWheelMoved(event);
	}

	/**
	 * Accessor for queueing keyboard events. Used by the NAppletManager since
	 * PApplet.enqueueKeyEvent() is protected.
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
		if (embeddedNApplet) {
			if (!nappletHidden) {
				loadPixels();
				parentPApplet.tint(nappletTint);
				parentPApplet.image(this.g, nappletX, nappletY);
			}
		} else
			super.paint();
	}

	/**
	 * Overrides PApplet.delay(). For now, this just means delay() is disabled
	 * for embedded NApplets. For standalone NApplets, this just passes through
	 * to PApplet.delay().
	 */
	public void delay(int napTime) {
		if (embeddedNApplet || windowedNApplet)
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
		if (embeddedNApplet || windowedNApplet)
			System.err
					.println("NApplet: frameRate(float newRateTarget) disabled.");
		else
			super.frameRate(newRateTarget);
	}

	// Disabled cursor manipulation for embedded NApplets for now. Will probably
	// bring it back at some point, but it'll be tricky to manage it properly
	// for embedded NApplets.

	/**
	 * Cursor manipulation disabled for now for embedded NApplets. Should work
	 * fine for windowed or standalone NApplets (though I haven't tested that.)
	 */
	public void cursor(int cursorType) {
		if (embeddedNApplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.cursor(cursorType);
	}

	/**
	 * Cursor manipulation disabled for now for embedded NApplets. Should work
	 * fine for windowed or standalone NApplets (though I haven't tested that.)
	 */
	public void cursor(PImage image) {
		if (embeddedNApplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.cursor(image);
	}

	/**
	 * Cursor manipulation disabled for now for embedded NApplets. Should work
	 * fine for windowed or standalone NApplets (though I haven't tested that.)
	 */
	public void cursor(PImage image, int hotspotX, int hotspotY) {
		if (embeddedNApplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.cursor(image, hotspotX, hotspotY);
	}

	/**
	 * Cursor manipulation disabled for now for embedded NApplets. Should work
	 * fine for windowed or standalone NApplets (though I haven't tested that.)
	 */
	public void cursor() {
		if (embeddedNApplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.cursor();
	}

	/**
	 * Cursor manipulation disabled for now for embedded NApplets. Should work
	 * fine for windowed or standalone NApplets (though I haven't tested that.)
	 */
	public void noCursor() {
		if (embeddedNApplet)
			System.err
					.println("NApplet: Cursor manipulation disabled for now.");
		else
			super.noCursor();
	}

	/**
	 * NApplet factory method.
	 * 
	 * @param parent
	 *            Parent PApplet or NApplet for the new NApplet.
	 * @param nappletClassName
	 *            Name of the new NApplet's class.
	 * @return The created NApplet.
	 */
	public static NApplet createNApplet(PApplet parent,
			String nappletClassName, NitManager nappletManager) {
		Class<?> nappletClass = null;
		Constructor<?> constructor = null;
		Class<?>[] constructorParams = {};
		Object[] constructorArgs = {};
		NApplet napplet = null;

		try {
			nappletClass = Class.forName(nappletClassName);
		} catch (ClassNotFoundException e) {
			try {
				nappletClass = Class.forName(parent.getClass().getName() + "$"
						+ nappletClassName);
			} catch (ClassNotFoundException e1) {
				String pcName = parent.getClass().getName();
				try {
					nappletClass = Class.forName(pcName.substring(0, pcName
							.lastIndexOf('.'))
							+ "." + nappletClassName);
				} catch (ClassNotFoundException e2) {
					System.err
							.println("NApplet.createNapplet(): Class not found.");
					e2.printStackTrace();
				}
			}
		}

		if (nappletClass != null) {
			if (nappletClass.getName().contains("$")) {
				constructorParams = new Class[] { parent.getClass() };
				constructorArgs = new Object[] { parent };
			}
			try {
				constructor = nappletClass.getConstructor(constructorParams);
			} catch (Exception e) {
				System.err
						.println("NApplet.createNApplet(): Constructor access error.");
				e.printStackTrace();
			}
		}

		if (constructor != null) {
			try {
				napplet = (NApplet) constructor.newInstance(constructorArgs);
			} catch (Exception e) {
				System.err
						.println("NApplet.createNApplet(): Object instantiation error.");
				e.printStackTrace();
			}
		}

		napplet.nitManager = nappletManager;
		return napplet;
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		pmouseWheel = mouseWheel;
		mouseWheel += e.getWheelRotation();
		mouseWheelMoved();
	}

	public void mouseWheelMoved() {

	}

}