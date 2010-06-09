package napplet;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
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
public class NApplet extends PApplet implements Nit, MouseWheelListener,
		ComponentListener {

	/**
	 * Library version.
	 */
	public static final String VERSION = "0.3.1";

	/**
	 * Returns library version.
	 * 
	 * @return version
	 */
	public String version() {
		return VERSION;
	}

	/**
	 * Time in milliseconds when the applet was started. We need to have our own
	 * number for this since PApplet.millisOffset is private.
	 */
	long millisOffset;

	/**
	 * Overrides PApplet's millis() routine.
	 */
	public int millis() {
		if (embeddedNApplet || windowedNApplet)
			return (int) (parentPApplet.millis() - millisOffset);
		else
			return super.millis();
	}

	// New members for Napplet

	/**
	 * The PApplet (or NApplet) that this NApplet is displayed on, if any.
	 */
	public PApplet parentPApplet = null;

	/**
	 * The object managing this NApplet, i.e., a NAppletManager being run by the
	 * parent PApplet. Remains null if this NApplet is being run on its own,
	 * otherwise the manager will set it.
	 */
	public NAppletManager parentNAppletManager = null;

	/**
	 * Manager for this NApplet's contained Nits (e.g., NApplets).
	 */
	public NAppletManager nappletManager;

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

	protected boolean resizeRequest = false;
	protected int resizeWidth;
	protected int resizeHeight;

	static public class NAppletRendererChangeException extends
			RendererChangeException {
	}

	/**
	 * Base constructor. Use initEmbeddedNApplet() or initWindowedNApplet() to
	 * initialize a NApplet. Override this to have a custom constructor for a
	 * subclass using parameters (e.g., to initialize fields).
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
	 *            later on for napplet-izing an existing processing sketch. I
	 *            need to write a tool for this).
	 */
	protected void initNApplet(PApplet pap, int x, int y, String sketchPath) {
		parentPApplet = pap;
		// Have to do this because PApplet.millisOffset is private.
		millisOffset = parentPApplet.millis();
		online = parentPApplet.online;

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

		// Transplanted from PApplet.init().
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

		nappletManager = new NAppletManager(this);

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

	/**
	 * Calls PApplet.addListeners(), and additionally adds a listener for the
	 * mousewheel.
	 * 
	 * @see processing.core.PApplet#addListeners()
	 */
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
	 * @see napplet.Nit#getPositionX()
	 */
	public int getPositionX() {
		return nappletX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#getPositionY()
	 */
	public int getPositionY() {
		return nappletY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#setPosition(int, int)
	 */
	public void setPosition(int x, int y) {
		nappletX = x;
		nappletY = y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#getWidth()
	 */
	public int getWidth() {
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#getHeight()
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns true if the NApplet is currently hidden from view by the
	 * nappletHidden variable.
	 * 
	 * @return true if the NApplet is hidden.
	 */
	public boolean isHidden() {
		return nappletHidden;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#isEmbedded()
	 */
	public boolean isEmbedded() {
		return embeddedNApplet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#runFrame()
	 */
	public void runFrame() {
		if (resizeRequest) {
			//resizeRenderer(resizeWidth, resizeHeight);
			resizeRequest = false;
			windowResized();
		}
		handleDraw();
	}

	public void handleDraw() {
		
			super.handleDraw();
		
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
	 * Sets a windowed or stand-alone NApplet to be user-resizable. Has no
	 * effect on an embedded NApplet.
	 * 
	 * When a NApplet is resized, it will call the windowResized() method, which
	 * is empty by default and can be overriden with whatever you like.
	 * 
	 * @param resizable
	 *            true if the window should be user-resizable.
	 */
	public void setResizable(boolean resizable) {
		if (!embeddedNApplet)
			frame.setResizable(resizable);
	}

	/**
	 * Override for PApplet.exit(). Handles things for a windowed or embedded
	 * NApplet, or falls through to PApplet.exit() for a standalone.
	 */
	public void exit() {
		if (windowedNApplet)
			frame.dispose();
		if (embeddedNApplet || windowedNApplet)
			parentNAppletManager.killNit(this);
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
			super.size(iwidth, iheight, irenderer, ipath);
		}
	}

	@Override
	public void resize(int iwidth, int iheight) {
		resizeRequest = true;
		resizeWidth = iwidth;
		resizeHeight = iheight;
	}

	/**
	 * Overrides PApplet's colorMode(int mode) method.
	 * 
	 * For some reason, a NApplet that called colorMode(HSB) in its setup()
	 * without specifying a max range for the color values would have those
	 * ranges set to zero. (The NApplet version of the FireCube demo had this
	 * problem.) This method intercepts a call to colorMode with no range
	 * specification, checks to see if g currently has any ranges specified,
	 * fills any missing ranges in with 255, and then calls
	 * PApplet.colorMode(int, float, float, float, float) to set them
	 * explicitly.
	 * 
	 * @see processing.core.PApplet#colorMode(int)
	 */
	@Override
	public void colorMode(int mode) {

		// Need to make sure sensible values get passed for the max color ranges
		// if they aren't set already. (I'm not sure why they don't get set, but
		// whatever.)

		float maxX = (g.colorModeX == 0.0) ? 255 : g.colorModeX;
		float maxY = (g.colorModeY == 0.0) ? 255 : g.colorModeY;
		float maxZ = (g.colorModeZ == 0.0) ? 255 : g.colorModeZ;
		float maxA = (g.colorModeA == 0.0) ? 255 : g.colorModeA;

		super.colorMode(mode, maxX, maxY, maxZ, maxA);
		
	}

	/**
	 * Accessor for queueing mouse and keyboard events. Used by the
	 * NAppletManager since PApplet.enqueueXXXXEvent() is protected.
	 * 
	 * @param event
	 *            Input event to be queued. Mouse locations need to be
	 *            translated to the NApplet's local coordinates.
	 */
	public void passEvent(InputEvent event) {
		if (event instanceof MouseWheelEvent) {
			if (nappletManager != null)
				nappletManager.handleMouseWheelEvent((MouseWheelEvent) event);
			else
				this.mouseWheelMoved((MouseWheelEvent) event);
		} else if (event instanceof MouseEvent) {
			if (nappletManager != null)
				nappletManager.handleMouseEvent((MouseEvent) event);
			else
				this.mousePressed((MouseEvent) event);
		} else if (event instanceof java.awt.event.KeyEvent) {
			if (nappletManager != null)
				nappletManager.handleKeyEvent((KeyEvent) event);
			else
				this.keyPressed((KeyEvent) event);
		}
	}

	/**
	 * Overrides PApplet.paint(). If the NApplet is embedded, uses the
	 * PApplet.image() method to paint the NApplet's pixels into the parent's
	 * display. Otherwise, just falls through to PApplet.paint().
	 */
	protected void paint() {
		if (embeddedNApplet) {
			if (!nappletHidden) {
				loadPixels();
				parentPApplet.tint(nappletTint);
				parentPApplet.image(this.g, 0, 0);
			}
		} else
			super.paint();
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
			String nappletClassName, NAppletManager nappletManager) {
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

		napplet.setNAppletManager(nappletManager);
		// napplet.parentNAppletManager = nappletManager;
		return napplet;
	}

	/**
	 * Called when the mousewheel is moved. Meant to be overridden a la
	 * mouseMoved(), mousePressed(), etc.
	 */
	public void mouseWheelMoved() {
	}

	/**
	 * Called when the window is resized. Override this when you want the
	 * NApplet to do something after a resize.
	 */
	public void windowResized() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#getNAppletManager()
	 */
	@Override
	public NAppletManager getNAppletManager() {
		return parentNAppletManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#setNAppletManager(napplet.NAppletManager)
	 */
	@Override
	public void setNAppletManager(NAppletManager nappletManager) {
		this.parentNAppletManager = nappletManager;
		this.parentPApplet = nappletManager.parentPApplet;
		millisOffset = parentPApplet.millis();
		online = parentPApplet.online;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see napplet.Nit#inputHit(int, int)
	 */
	@Override
	public boolean inputHit(int x, int y) {
		return true;
	}

	// Overrides for methods inherited from PApplet that are disabled for
	// NApplets because they're either inappropriate or too tricky.

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

	// Listener methods (as with listener methods for PApplet, override these at
	// your own risk.)

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
	 * MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		pmouseWheel = mouseWheel;
		mouseWheel += e.getWheelRotation();
		mouseWheelMoved();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.
	 * ComponentEvent)
	 */
	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
	 * )
	 */
	@Override
	public void componentMoved(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.
	 * ComponentEvent)
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		java.awt.Insets insets = frame.getInsets();
		int iwidth = e.getComponent().getWidth() - (insets.left + insets.right);
		int iheight = e.getComponent().getHeight()
				- (insets.top + insets.bottom);
		resizeRenderer(iwidth, iheight);
		windowResized();
		// resize(iwidth, iheight);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent
	 * )
	 */
	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public Frame getFrame() {
		return this.frame;
	}

}