package napplet;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import processing.core.PApplet;
import processing.core.PImage;



@SuppressWarnings("serial")
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

	  Thread thread;

	  // New members for Napplet
	  
	  PApplet parentPApplet;
	  int nappletX;
	  int nappletY;
	  boolean embeddedNapplet = false;
	  
	  public Napplet() {}

	  /**
	   * Replaces call to init() for sub-sketch Napplets (I hope.)
	   */
	  public void nappletInit(PApplet pap, int x, int y, int w, int h, String skPath) {

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
	  
	  public void init() {
		  super.init();
	  }
	  
	  /**
	   * Called by the browser or applet viewer to inform this applet that it
	   * should start its execution. It is called after the init method and
	   * each time the applet is revisited in a Web page.
	   * <p/>
	   * Called explicitly via the first call to PApplet.paint(), because
	   * PAppletGL needs to have a usable screen before getting things rolling.
	   */
	  public void start() {
	    // When running inside a browser, start() will be called when someone
	    // returns to a page containing this applet.
	    // http://dev.processing.org/bugs/show_bug.cgi?id=581
//	    finished = false;
//
//	    if (thread != null) return;
//	    thread = new Thread(this, "Animation Thread");
//	    thread.start();
		  super.start();
	  }


	  /**
	   * Called by the browser or applet viewer to inform
	   * this applet that it should stop its execution.
	   * <p/>
	   * Unfortunately, there are no guarantees from the Java spec
	   * when or if stop() will be called (i.e. on browser quit,
	   * or when moving between web pages), and it's not always called.
	   */
	  public void stop() {
	    // bringing this back for 0111, hoping it'll help opengl shutdown
	    finished = true;  // why did i comment this out?

	    // don't run stop and disposers twice
	    if (thread == null) return;
	    thread = null;

	    // call to shut down renderer, in case it needs it (pdf does)
	    if (g != null) g.dispose();

	    // maybe this should be done earlier? might help ensure it gets called
	    // before the vm just craps out since 1.5 craps out so aggressively.
	    disposeMethods.handle();
	  }


	  /**
	   * Called by the browser or applet viewer to inform this applet
	   * that it is being reclaimed and that it should destroy
	   * any resources that it has allocated.
	   * <p/>
	   * This also attempts to call PApplet.stop(), in case there
	   * was an inadvertent override of the stop() function by a user.
	   * <p/>
	   * destroy() supposedly gets called as the applet viewer
	   * is shutting down the applet. stop() is called
	   * first, and then destroy() to really get rid of things.
	   * no guarantees on when they're run (on browser quit, or
	   * when moving between pages), though.
	   */
	  public void destroy() {
	    ((PApplet)this).stop();
	  }



	  //////////////////////////////////////////////////////////////


	  public void setup() {
	  }


	  public void draw() {
	    // if no draw method, then shut things down
	    //System.out.println("no draw method, goodbye");
	    finished = true;
	  }


	  //////////////////////////////////////////////////////////////


	  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


	  public void update(Graphics screen) {
	    paint(screen);
	  }


	  //synchronized public void paint(Graphics screen) {  // shutting off for 0146
	  public void paint(Graphics screen) {
	    // ignore the very first call to paint, since it's coming
	    // from the o.s., and the applet will soon update itself anyway.
	    if (frameCount == 0) {
//	      println("Skipping frame");
	      // paint() may be called more than once before things
	      // are finally painted to the screen and the thread gets going
	      return;
	    }

	    // without ignoring the first call, the first several frames
	    // are confused because paint() gets called in the midst of
	    // the initial nextFrame() call, so there are multiple
	    // updates fighting with one another.

	    // g.image is synchronized so that draw/loop and paint don't
	    // try to fight over it. this was causing a randomized slowdown
	    // that would cut the frameRate into a third on macosx,
	    // and is probably related to the windows sluggishness bug too

	    // make sure the screen is visible and usable
	    // (also prevents over-drawing when using PGraphicsOpenGL)
	    if ((g != null) && (g.image != null)) {
//	      println("inside paint(), screen.drawImage()");
	      screen.drawImage(g.image, 0, 0, null);
	    }
	  }


	  // active paint method
	  protected void paint() {
		  if (embeddedNapplet) {
			  loadPixels();
			  parentPApplet.image(this.g, 50, 50);
		  }
		  else 
			  super.paint();
	  }


	  //////////////////////////////////////////////////////////////


	  /**
	   * Main method for the primary animation thread.
	   *
	   * <A HREF="http://java.sun.com/products/jfc/tsc/articles/painting/">Painting in AWT and Swing</A>
	   */
	  public void run() {  // not good to make this synchronized, locks things up
	    long beforeTime = System.nanoTime();
	    long overSleepTime = 0L;

	    int noDelays = 0;
	    // Number of frames with a delay of 0 ms before the
	    // animation thread yields to other running threads.
	    final int NO_DELAYS_PER_YIELD = 15;

	    /*
	      // this has to be called after the exception is thrown,
	      // otherwise the supporting libs won't have a valid context to draw to
	      Object methodArgs[] =
	        new Object[] { new Integer(width), new Integer(height) };
	      sizeMethods.handle(methodArgs);
	     */

	    while ((Thread.currentThread() == thread) && !finished) {
	      // Don't resize the renderer from the EDT (i.e. from a ComponentEvent),
	      // otherwise it may attempt a resize mid-render.
	      if (resizeRequest) {
	        resizeRenderer(resizeWidth, resizeHeight);
	        resizeRequest = false;
	      }

	      // render a single frame
	      handleDraw();

	      if (frameCount == 1) {
	        // Call the request focus event once the image is sure to be on
	        // screen and the component is valid. The OpenGL renderer will
	        // request focus for its canvas inside beginDraw().
	        // http://java.sun.com/j2se/1.4.2/docs/api/java/awt/doc-files/FocusSpec.html
	        //println("requesting focus");
	        requestFocus();
	      }

	      // wait for update & paint to happen before drawing next frame
	      // this is necessary since the drawing is sometimes in a
	      // separate thread, meaning that the next frame will start
	      // before the update/paint is completed

	      long afterTime = System.nanoTime();
	      long timeDiff = afterTime - beforeTime;
	      //System.out.println("time diff is " + timeDiff);
	      long sleepTime = (frameRatePeriod - timeDiff) - overSleepTime;

	      if (sleepTime > 0) {  // some time left in this cycle
	        try {
//	          Thread.sleep(sleepTime / 1000000L);  // nanoseconds -> milliseconds
	          Thread.sleep(sleepTime / 1000000L, (int) (sleepTime % 1000000L));
	          noDelays = 0;  // Got some sleep, not delaying anymore
	        } catch (InterruptedException ex) { }

	        overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
	        //System.out.println("  oversleep is " + overSleepTime);

	      } else {    // sleepTime <= 0; the frame took longer than the period
//	        excess -= sleepTime;  // store excess time value
	        overSleepTime = 0L;

	        if (noDelays > NO_DELAYS_PER_YIELD) {
	          Thread.yield();   // give another thread a chance to run
	          noDelays = 0;
	        }
	      }

	      beforeTime = System.nanoTime();
	    }

	    stop();  // call to shutdown libs?

	    // If the user called the exit() function, the window should close,
	    // rather than the sketch just halting.
	    if (exitCalled) {
	      exit2a();
	    }
	  }


	  //synchronized public void handleDisplay() {
	  public void handleDraw() {
	    if (g != null && (looping || redraw)) {
	      if (!g.canDraw()) {
	        // Don't draw if the renderer is not yet ready.
	        // (e.g. OpenGL has to wait for a peer to be on screen)
	        return;
	      }

	      //System.out.println("handleDraw() " + frameCount);

	      g.beginDraw();
	      if (recorder != null) {
	        recorder.beginDraw();
	      }

	      long now = System.nanoTime();

	      if (frameCount == 0) {
	        try {
	          //println("Calling setup()");
	          setup();
	          //println("Done with setup()");

	        } catch (RendererChangeException e) {
	          // Give up, instead set the new renderer and re-attempt setup()
	          return;
	        }
	        this.defaultSize = false;

	      } else {  // frameCount > 0, meaning an actual draw()
	        // update the current frameRate
	        double rate = 1000000.0 / ((now - frameRateLastNanos) / 1000000.0);
	        float instantaneousRate = (float) rate / 1000.0f;
	        frameRate = (frameRate * 0.9f) + (instantaneousRate * 0.1f);

	        preMethods.handle();

	        // use dmouseX/Y as previous mouse pos, since this is the
	        // last position the mouse was in during the previous draw.
	        pmouseX = dmouseX;
	        pmouseY = dmouseY;

	        //println("Calling draw()");
	        draw();
	        //println("Done calling draw()");

	        // dmouseX/Y is updated only once per frame (unlike emouseX/Y)
	        dmouseX = mouseX;
	        dmouseY = mouseY;

	        // these are called *after* loop so that valid
	        // drawing commands can be run inside them. it can't
	        // be before, since a call to background() would wipe
	        // out anything that had been drawn so far.
	        dequeueMouseEvents();
	        dequeueKeyEvents();

	        drawMethods.handle();

	        redraw = false;  // unset 'redraw' flag in case it was set
	        // (only do this once draw() has run, not just setup())

	      }

	      g.endDraw();
	      if (recorder != null) {
	        recorder.endDraw();
	      }

	      frameRateLastNanos = now;
	      frameCount++;

	      // Actively render the screen
	      paint();

//	    repaint();
//	    getToolkit().sync();  // force repaint now (proper method)

	      postMethods.handle();
	    }
	  }

	  //////////////////////////////////////////////////////////////


	  public void addListeners() {
	    addMouseListener(this);
	    addMouseMotionListener(this);
	    addKeyListener(this);
	    addFocusListener(this);

	    addComponentListener(new ComponentAdapter() {
	      public void componentResized(ComponentEvent e) {
	        Component c = e.getComponent();
	        //System.out.println("componentResized() " + c);
	        Rectangle bounds = c.getBounds();
	        resizeRequest = true;
	        resizeWidth = bounds.width;
	        resizeHeight = bounds.height;
	      }
	    });
	  }


	  //////////////////////////////////////////////////////////////

	  public void focusGained() { }

	  public void focusGained(FocusEvent e) {
	    focused = true;
	    focusGained();
	  }

	  public void focusLost() { }

	  public void focusLost(FocusEvent e) {
	    focused = false;
	    focusLost();
	  }

	  //////////////////////////////////////////////////////////////

	  // controlling time (playing god)

	  
	  public void delay(int napTime) {
		  System.err.println("Napplet: delay() disabled.");
	  }

	  public void frameRate(float newRateTarget) {
		  System.err.println("Napplet: frameRate(float newRateTarget) disabled.");
	  }

	  

	  //////////////////////////////////////////////////////////////


	  /**
	   * Function for an applet/application to kill itself and
	   * display an error. Mostly this is here to be improved later.
	   */
	  public void die(String what) {
	    stop();
	    throw new RuntimeException(what);
	  }


	  /**
	   * Same as above but with an exception. Also needs work.
	   */
	  public void die(String what, Exception e) {
	    if (e != null) e.printStackTrace();
	    die(what);
	  }


	  /**
	   * Call to safely exit the sketch when finished. For instance,
	   * to render a single frame, save it, and quit.
	   */
	  public void exit() {
	    if (thread == null) {
	      // exit immediately, stop() has already been called,
	      // meaning that the main thread has long since exited
	      exit2a();

	    } else if (looping) {
	      // stop() will be called as the thread exits
	      finished = true;
	      // tell the code to call exit2() to do a System.exit()
	      // once the next draw() has completed
	      exitCalled = true;

	    } else if (!looping) {
	      // if not looping, need to call stop explicitly,
	      // because the main thread will be sleeping
	      stop();

	      // now get out
	      exit2a();
	    }
	  }


	  void exit2a() {
	    try {
	      System.exit(0);
	    } catch (SecurityException e) {
	      // don't care about applet security exceptions
	    }
	  }



	  //////////////////////////////////////////////////////////////

	  public void thread(final String name) {
	    Thread later = new Thread() {
	      public void run() {
	        method(name);
	      }
	    };
	    later.start();
	  }


	  //////////////////////////////////////////////////////////////

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

	  //////////////////////////////////////////////////////////////

	  // MAIN


	  /**
	   * Set this sketch to communicate its state back to the PDE.
	   * <p/>
	   * This uses the stderr stream to write positions of the window
	   * (so that it will be saved by the PDE for the next run) and
	   * notify on quit. See more notes in the Worker class.
	   */
	  public void setupExternalMessages() {

	    frame.addComponentListener(new ComponentAdapter() {
	        public void componentMoved(ComponentEvent e) {
	          Point where = ((Frame) e.getSource()).getLocation();
	          System.err.println(PApplet.EXTERNAL_MOVE + " " +
	                             where.x + " " + where.y);
	          System.err.flush();  // doesn't seem to help or hurt
	        }
	      });

	    frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
//	          System.err.println(PApplet.EXTERNAL_QUIT);
//	          System.err.flush();  // important
//	          System.exit(0);
	          exit();  // don't quit, need to just shut everything down (0133)
	        }
	      });
	  }


	  /**
	   * Set up a listener that will fire proper component resize events
	   * in cases where frame.setResizable(true) is called.
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
	              int usableW = windowSize.width - insets.left - insets.right;
	              int usableH = windowSize.height - insets.top - insets.bottom;

	              // the ComponentListener in PApplet will handle calling size()
	              setBounds(insets.left, insets.top, usableW, usableH);
	            }
	          }
	        }
	      });
	  }

}