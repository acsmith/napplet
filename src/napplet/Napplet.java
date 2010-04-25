package napplet;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import processing.core.PApplet;
import processing.core.PGraphics;
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

	  protected RegisteredMethods sizeMethods;
	  protected RegisteredMethods preMethods, drawMethods, postMethods;
	  protected RegisteredMethods mouseEventMethods, keyEventMethods;
	  protected RegisteredMethods disposeMethods;

	  // messages to send if attached as an external vm

	  /** true if this sketch is being run by the PDE */
	  boolean external = false;


	  static final String ERROR_MIN_MAX =
	    "Cannot use min() or max() on an empty array.";


	  // during rev 0100 dev cycle, working on new threading model,
	  // but need to disable and go conservative with changes in order
	  // to get pdf and audio working properly first.
	  // for 0116, the CRUSTY_THREADS are being disabled to fix lots of bugs.
	  //static final boolean CRUSTY_THREADS = false; //true;


	  public void init() {
//	    println("Calling init()");

	    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	    screenWidth = screen.width;
	    screenHeight = screen.height;

	    // send tab keys through to the PApplet
	    setFocusTraversalKeysEnabled(false);

	    millisOffset = System.currentTimeMillis();

	    finished = false; // just for clarity

	    // this will be cleared by draw() if it is not overridden
	    looping = true;
	    redraw = true;  // draw this guy once
	    firstMouse = true;

	    // these need to be inited before setup
	    sizeMethods = new RegisteredMethods();
	    preMethods = new RegisteredMethods();
	    drawMethods = new RegisteredMethods();
	    postMethods = new RegisteredMethods();
	    mouseEventMethods = new RegisteredMethods();
	    keyEventMethods = new RegisteredMethods();
	    disposeMethods = new RegisteredMethods();

	    try {
	      getAppletContext();
	      online = true;
	    } catch (NullPointerException e) {
	      online = false;
	    }

	    try {
	      if (sketchPath == null) {
	        sketchPath = System.getProperty("user.dir");
	      }
	    } catch (Exception e) { }  // may be a security problem

	    Dimension size = getSize();
	    if ((size.width != 0) && (size.height != 0)) {
	      // When this PApplet is embedded inside a Java application with other
	      // Component objects, its size() may already be set externally (perhaps
	      // by a LayoutManager). In this case, honor that size as the default.
	      // Size of the component is set, just create a renderer.
	      g = makeGraphics(size.width, size.height, getSketchRenderer(), null, true);
	      // This doesn't call setSize() or setPreferredSize() because the fact
	      // that a size was already set means that someone is already doing it.

	    } else {
	      // Set the default size, until the user specifies otherwise
	      this.defaultSize = true;
	      int w = getSketchWidth();
	      int h = getSketchHeight();
	      g = makeGraphics(w, h, getSketchRenderer(), null, true);
	      // Fire component resize event
	      setSize(w, h);
	      setPreferredSize(new Dimension(w, h));
	    }
	    width = g.width;
	    height = g.height;

	    addListeners();

	    // this is automatically called in applets
	    // though it's here for applications anyway
	    start();
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
	    finished = false;

	    if (thread != null) return;
	    thread = new Thread(this, "Animation Thread");
	    thread.start();
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


	  /**
	   * This returns the last width and height specified by the user
	   * via the size() command.
	   */
	//  public Dimension getPreferredSize() {
//	    return new Dimension(width, height);
	//  }


	//  public void addNotify() {
//	    super.addNotify();
//	    println("addNotify()");
	//  }



	  //////////////////////////////////////////////////////////////


	  public class RegisteredMethods {
	    int count;
	    Object objects[];
	    Method methods[];


	    // convenience version for no args
	    public void handle() {
	      handle(new Object[] { });
	    }

	    public void handle(Object oargs[]) {
	      for (int i = 0; i < count; i++) {
	        try {
	          //System.out.println(objects[i] + " " + args);
	          methods[i].invoke(objects[i], oargs);
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	      }
	    }

	    public void add(Object object, Method method) {
	      if (objects == null) {
	        objects = new Object[5];
	        methods = new Method[5];
	      }
	      if (count == objects.length) {
	        objects = (Object[]) PApplet.expand(objects);
	        methods = (Method[]) PApplet.expand(methods);
//	        Object otemp[] = new Object[count << 1];
//	        System.arraycopy(objects, 0, otemp, 0, count);
//	        objects = otemp;
//	        Method mtemp[] = new Method[count << 1];
//	        System.arraycopy(methods, 0, mtemp, 0, count);
//	        methods = mtemp;
	      }
	      objects[count] = object;
	      methods[count] = method;
	      count++;
	    }


	    /**
	     * Removes first object/method pair matched (and only the first,
	     * must be called multiple times if object is registered multiple times).
	     * Does not shrink array afterwards, silently returns if method not found.
	     */
	    public void remove(Object object, Method method) {
	      int index = findIndex(object, method);
	      if (index != -1) {
	        // shift remaining methods by one to preserve ordering
	        count--;
	        for (int i = index; i < count; i++) {
	          objects[i] = objects[i+1];
	          methods[i] = methods[i+1];
	        }
	        // clean things out for the gc's sake
	        objects[count] = null;
	        methods[count] = null;
	      }
	    }

	    protected int findIndex(Object object, Method method) {
	      for (int i = 0; i < count; i++) {
	        if (objects[i] == object && methods[i].equals(method)) {
	          //objects[i].equals() might be overridden, so use == for safety
	          // since here we do care about actual object identity
	          //methods[i]==method is never true even for same method, so must use
	          // equals(), this should be safe because of object identity
	          return i;
	        }
	      }
	      return -1;
	    }
	  }


	  public void registerSize(Object o) {
	    Class<?> methodArgs[] = new Class[] { Integer.TYPE, Integer.TYPE };
	    registerWithArgs(sizeMethods, "size", o, methodArgs);
	  }

	  public void registerPre(Object o) {
	    registerNoArgs(preMethods, "pre", o);
	  }

	  public void registerDraw(Object o) {
	    registerNoArgs(drawMethods, "draw", o);
	  }

	  public void registerPost(Object o) {
	    registerNoArgs(postMethods, "post", o);
	  }

	  public void registerMouseEvent(Object o) {
	    Class<?> methodArgs[] = new Class[] { MouseEvent.class };
	    registerWithArgs(mouseEventMethods, "mouseEvent", o, methodArgs);
	  }


	  public void registerKeyEvent(Object o) {
	    Class<?> methodArgs[] = new Class[] { KeyEvent.class };
	    registerWithArgs(keyEventMethods, "keyEvent", o, methodArgs);
	  }

	  public void registerDispose(Object o) {
	    registerNoArgs(disposeMethods, "dispose", o);
	  }


	  protected void registerNoArgs(RegisteredMethods meth,
	                                String name, Object o) {
	    Class<?> c = o.getClass();
	    try {
	      Method method = c.getMethod(name, new Class[] {});
	      meth.add(o, method);

	    } catch (NoSuchMethodException nsme) {
	      die("There is no public " + name + "() method in the class " +
	          o.getClass().getName());

	    } catch (Exception e) {
	      die("Could not register " + name + " + () for " + o, e);
	    }
	  }


	  protected void registerWithArgs(RegisteredMethods meth,
	                                  String name, Object o, Class<?> cargs[]) {
	    Class<?> c = o.getClass();
	    try {
	      Method method = c.getMethod(name, cargs);
	      meth.add(o, method);

	    } catch (NoSuchMethodException nsme) {
	      die("There is no public " + name + "() method in the class " +
	          o.getClass().getName());

	    } catch (Exception e) {
	      die("Could not register " + name + " + () for " + o, e);
	    }
	  }


	  public void unregisterSize(Object o) {
	    Class<?> methodArgs[] = new Class[] { Integer.TYPE, Integer.TYPE };
	    unregisterWithArgs(sizeMethods, "size", o, methodArgs);
	  }

	  public void unregisterPre(Object o) {
	    unregisterNoArgs(preMethods, "pre", o);
	  }

	  public void unregisterDraw(Object o) {
	    unregisterNoArgs(drawMethods, "draw", o);
	  }

	  public void unregisterPost(Object o) {
	    unregisterNoArgs(postMethods, "post", o);
	  }

	  public void unregisterMouseEvent(Object o) {
	    Class<?> methodArgs[] = new Class[] { MouseEvent.class };
	    unregisterWithArgs(mouseEventMethods, "mouseEvent", o, methodArgs);
	  }

	  public void unregisterKeyEvent(Object o) {
	    Class<?> methodArgs[] = new Class[] { KeyEvent.class };
	    unregisterWithArgs(keyEventMethods, "keyEvent", o, methodArgs);
	  }

	  public void unregisterDispose(Object o) {
	    unregisterNoArgs(disposeMethods, "dispose", o);
	  }


	  protected void unregisterNoArgs(RegisteredMethods meth,
	                                  String name, Object o) {
	    Class<?> c = o.getClass();
	    try {
	      Method method = c.getMethod(name, new Class[] {});
	      meth.remove(o, method);
	    } catch (Exception e) {
	      die("Could not unregister " + name + "() for " + o, e);
	    }
	  }


	  protected void unregisterWithArgs(RegisteredMethods meth,
	                                    String name, Object o, Class<?> cargs[]) {
	    Class<?> c = o.getClass();
	    try {
	      Method method = c.getMethod(name, cargs);
	      meth.remove(o, method);
	    } catch (Exception e) {
	      die("Could not unregister " + name + "() for " + o, e);
	    }
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

	  /**
	   * Defines the dimension of the display window in units of pixels. The <b>size()</b> function <em>must</em> be the first line in <b>setup()</b>. If <b>size()</b> is not called, the default size of the window is 100x100 pixels. The system variables <b>width</b> and <b>height</b> are set by the parameters passed to the <b>size()</b> function. <br><br>
	   * Do not use variables as the parameters to <b>size()</b> command, because it will cause problems when exporting your sketch. When variables are used, the dimensions of your sketch cannot be determined during export. Instead, employ numeric values in the <b>size()</b> statement, and then use the built-in <b>width</b> and <b>height</b> variables inside your program when you need the dimensions of the display window are needed. <br><br>
	   * The MODE parameters selects which rendering engine to use. For example, if you will be drawing 3D shapes for the web use <b>P3D</b>, if you want to export a program with OpenGL graphics acceleration use <b>OPENGL</b>. A brief description of the four primary renderers follows:<br><br><b>JAVA2D</b> - The default renderer. This renderer supports two dimensional drawing and provides higher image quality in overall, but generally slower than P2D.<br><br><b>P2D</b> (Processing 2D) - Fast 2D renderer, best used with pixel data, but not as accurate as the JAVA2D default. <br><br><b>P3D</b> (Processing 3D) - Fast 3D renderer for the web. Sacrifices rendering quality for quick 3D drawing.<br><br><b>OPENGL</b> - High speed 3D graphics renderer that makes use of OpenGL-compatible graphics hardware is available. Keep in mind that OpenGL is not magic pixie dust that makes any sketch faster (though it's close), so other rendering options may produce better results depending on the nature of your code. Also note that with OpenGL, all graphics are smoothed: the smooth() and noSmooth() commands are ignored. <br><br><b>PDF</b> - The PDF renderer draws 2D graphics directly to an Acrobat PDF file. This produces excellent results when you need vector shapes for high resolution output or printing. You must first use Import Library &rarr; PDF to make use of the library. More information can be found in the PDF library reference.
	   * If you're manipulating pixels (using methods like get() or blend(), or manipulating the pixels[] array), P2D and P3D will usually be faster than the default (JAVA2D) setting, and often the OPENGL setting as well. Similarly, when handling lots of images, or doing video playback, P2D and P3D will tend to be faster.<br><br>
	   * The P2D, P3D, and OPENGL renderers do not support strokeCap() or strokeJoin(), which can lead to ugly results when using strokeWeight(). (<a href="http://dev.processing.org/bugs/show_bug.cgi?id=955">Bug 955</a>) <br><br>
	   * For the most elegant and accurate results when drawing in 2D, particularly when using smooth(), use the JAVA2D renderer setting. It may be slower than the others, but is the most complete, which is why it's the default. Advanced users will want to switch to other renderers as they learn the tradeoffs. <br><br>
	   * Rendering graphics requires tradeoffs between speed, accuracy, and general usefulness of the available features. None of the renderers are perfect, so we provide multiple options so that you can decide what tradeoffs make the most sense for your project. We'd prefer all of them to have perfect visual accuracy, high performance, and support a wide range of features, but that's simply not possible. <br><br>
	   * The maximum width and height is limited by your operating system, and is usually the width and height of your actual screen. On some machines it may simply be the number of pixels on your current screen, meaning that a screen that's 800x600 could support size(1600, 300), since it's the same number of pixels. This varies widely so you'll have to try different rendering modes and sizes until you get what you're looking for. If you need something larger, use <b>createGraphics</b> to create a non-visible drawing surface.
	   * <br><br>Again, the size() method must be the first line of the code (or first item inside setup). Any code that appears before the size() command may run more than once, which can lead to confusing results.
	   *
	   * =advanced
	   * Starts up and creates a two-dimensional drawing surface,
	   * or resizes the current drawing surface.
	   * <P>
	   * This should be the first thing called inside of setup().
	   * <P>
	   * If using Java 1.3 or later, this will default to using
	   * PGraphics2, the Java2D-based renderer. If using Java 1.1,
	   * or if PGraphics2 is not available, then PGraphics will be used.
	   * To set your own renderer, use the other version of the size()
	   * method that takes a renderer as its last parameter.
	   * <P>
	   * If called once a renderer has already been set, this will
	   * use the previous renderer and simply resize it.
	   *
	   * @webref structure
	   * @param iwidth width of the display window in units of pixels
	   * @param iheight height of the display window in units of pixels
	   */
	  public void size(int iwidth, int iheight) {
	    size(iwidth, iheight, JAVA2D, null);
	  }

	  /**
	   *
	   * @param irenderer   Either P2D, P3D, JAVA2D, or OPENGL
	   */
	  public void size(int iwidth, int iheight, String irenderer) {
	    size(iwidth, iheight, irenderer, null);
	  }


	  /**
	   * Creates a new PGraphics object and sets it to the specified size.
	   *
	   * Note that you cannot change the renderer once outside of setup().
	   * In most cases, you can call size() to give it a new size,
	   * but you need to always ask for the same renderer, otherwise
	   * you're gonna run into trouble.
	   *
	   * The size() method should *only* be called from inside the setup() or
	   * draw() methods, so that it is properly run on the main animation thread.
	   * To change the size of a PApplet externally, use setSize(), which will
	   * update the component size, and queue a resize of the renderer as well.
	   */
	  public void size(final int iwidth, final int iheight,
	                   String irenderer, String ipath) {
	    // Run this from the EDT, just cuz it's AWT stuff (or maybe later Swing)
	    SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
	        // Set the preferred size so that the layout managers can handle it
	        setPreferredSize(new Dimension(iwidth, iheight));
	        setSize(iwidth, iheight);
	      }
	    });

	    // ensure that this is an absolute path
	    if (ipath != null) ipath = savePath(ipath);

	    String currentRenderer = g.getClass().getName();
	    if (currentRenderer.equals(irenderer)) {
	      // Avoid infinite loop of throwing exception to reset renderer
	      resizeRenderer(iwidth, iheight);
	      //redraw();  // will only be called insize draw()

	    } else {  // renderer is being changed
	      // otherwise ok to fall through and create renderer below
	      // the renderer is changing, so need to create a new object
	      g = makeGraphics(iwidth, iheight, irenderer, ipath, true);
	      width = iwidth;
	      height = iheight;

	      // fire resize event to make sure the applet is the proper size
//	      setSize(iwidth, iheight);
	      // this is the function that will run if the user does their own
	      // size() command inside setup, so set defaultSize to false.
	      defaultSize = false;

	      // throw an exception so that setup() is called again
	      // but with a properly sized render
	      // this is for opengl, which needs a valid, properly sized
	      // display before calling anything inside setup().
	      throw new RendererChangeException();
	    }
	  }


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
	    try {
	      Graphics screen = this.getGraphics();
	      if (screen != null) {
	        if ((g != null) && (g.image != null)) {
	          screen.drawImage(g.image, 0, 0, null);
	        }
	        Toolkit.getDefaultToolkit().sync();
	      }
	    } catch (Exception e) {
	      // Seen on applet destroy, maybe can ignore?
	      e.printStackTrace();

//	    } finally {
//	      if (g != null) {
//	        g.dispose();
//	      }
	    }
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
	      exit2();
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



	  synchronized public void redraw() {
	    if (!looping) {
	      redraw = true;
//	      if (thread != null) {
//	        // wake from sleep (necessary otherwise it'll be
//	        // up to 10 seconds before update)
//	        if (CRUSTY_THREADS) {
//	          thread.interrupt();
//	        } else {
//	          synchronized (blocker) {
//	            blocker.notifyAll();
//	          }
//	        }
//	      }
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


	  MouseEvent mouseEventQueue[] = new MouseEvent[10];
	  int mouseEventCount;

	  protected void enqueueMouseEvent(MouseEvent e) {
	    synchronized (mouseEventQueue) {
	      if (mouseEventCount == mouseEventQueue.length) {
	        MouseEvent temp[] = new MouseEvent[mouseEventCount << 1];
	        System.arraycopy(mouseEventQueue, 0, temp, 0, mouseEventCount);
	        mouseEventQueue = temp;
	      }
	      mouseEventQueue[mouseEventCount++] = e;
	    }
	  }

	  protected void dequeueMouseEvents() {
	    synchronized (mouseEventQueue) {
	      for (int i = 0; i < mouseEventCount; i++) {
	        mouseEvent = mouseEventQueue[i];
	        handleMouseEvent(mouseEvent);
	      }
	      mouseEventCount = 0;
	    }
	  }


	  /**
	   * Actually take action based on a mouse event.
	   * Internally updates mouseX, mouseY, mousePressed, and mouseEvent.
	   * Then it calls the event type with no params,
	   * i.e. mousePressed() or mouseReleased() that the user may have
	   * overloaded to do something more useful.
	   */
	  protected void handleMouseEvent(MouseEvent event) {
	    int id = event.getID();

	    // http://dev.processing.org/bugs/show_bug.cgi?id=170
	    // also prevents mouseExited() on the mac from hosing the mouse
	    // position, because x/y are bizarre values on the exit event.
	    // see also the id check below.. both of these go together
	    if ((id == MouseEvent.MOUSE_DRAGGED) ||
	        (id == MouseEvent.MOUSE_MOVED)) {
	      pmouseX = emouseX;
	      pmouseY = emouseY;
	      mouseX = event.getX();
	      mouseY = event.getY();
	    }

	    mouseEvent = event;

	    int modifiers = event.getModifiers();
	    if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
	      mouseButton = LEFT;
	    } else if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
	      mouseButton = CENTER;
	    } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
	      mouseButton = RIGHT;
	    }
	    // if running on macos, allow ctrl-click as right mouse
	    if (platform == MACOSX) {
	      if (mouseEvent.isPopupTrigger()) {
	        mouseButton = RIGHT;
	      }
	    }

	    mouseEventMethods.handle(new Object[] { event });

	    // this used to only be called on mouseMoved and mouseDragged
	    // change it back if people run into trouble
	    if (firstMouse) {
	      pmouseX = mouseX;
	      pmouseY = mouseY;
	      dmouseX = mouseX;
	      dmouseY = mouseY;
	      firstMouse = false;
	    }

	    //println(event);

	    switch (id) {
	    case MouseEvent.MOUSE_PRESSED:
	      mousePressed = true;
	      mousePressed();
	      break;
	    case MouseEvent.MOUSE_RELEASED:
	      mousePressed = false;
	      mouseReleased();
	      break;
	    case MouseEvent.MOUSE_CLICKED:
	      mouseClicked();
	      break;
	    case MouseEvent.MOUSE_DRAGGED:
	      mouseDragged();
	      break;
	    case MouseEvent.MOUSE_MOVED:
	      mouseMoved();
	      break;
	    }

	    if ((id == MouseEvent.MOUSE_DRAGGED) ||
	        (id == MouseEvent.MOUSE_MOVED)) {
	      emouseX = mouseX;
	      emouseY = mouseY;
	    }
	  }


	  /**
	   * Figure out how to process a mouse event. When loop() has been
	   * called, the events will be queued up until drawing is complete.
	   * If noLoop() has been called, then events will happen immediately.
	   */
	  protected void checkMouseEvent(MouseEvent event) {
	    if (looping) {
	      enqueueMouseEvent(event);
	    } else {
	      handleMouseEvent(event);
	    }
	  }


	  /**
	   * If you override this or any function that takes a "MouseEvent e"
	   * without calling its super.mouseXxxx() then mouseX, mouseY,
	   * mousePressed, and mouseEvent will no longer be set.
	   */
	  public void mousePressed(MouseEvent e) {
	    checkMouseEvent(e);
	  }

	  public void mouseReleased(MouseEvent e) {
	    checkMouseEvent(e);
	  }

	  public void mouseClicked(MouseEvent e) {
	    checkMouseEvent(e);
	  }

	  public void mouseEntered(MouseEvent e) {
	    checkMouseEvent(e);
	  }

	  public void mouseExited(MouseEvent e) {
	    checkMouseEvent(e);
	  }

	  public void mouseDragged(MouseEvent e) {
	    checkMouseEvent(e);
	  }

	  public void mouseMoved(MouseEvent e) {
	    checkMouseEvent(e);
	  }

	  //////////////////////////////////////////////////////////////

	  KeyEvent keyEventQueue[] = new KeyEvent[10];
	  int keyEventCount;

	  protected void enqueueKeyEvent(KeyEvent e) {
	    synchronized (keyEventQueue) {
	      if (keyEventCount == keyEventQueue.length) {
	        KeyEvent temp[] = new KeyEvent[keyEventCount << 1];
	        System.arraycopy(keyEventQueue, 0, temp, 0, keyEventCount);
	        keyEventQueue = temp;
	      }
	      keyEventQueue[keyEventCount++] = e;
	    }
	  }

	  protected void dequeueKeyEvents() {
	    synchronized (keyEventQueue) {
	      for (int i = 0; i < keyEventCount; i++) {
	        keyEvent = keyEventQueue[i];
	        handleKeyEvent(keyEvent);
	      }
	      keyEventCount = 0;
	    }
	  }


	  protected void handleKeyEvent(KeyEvent event) {
	    keyEvent = event;
	    key = event.getKeyChar();
	    keyCode = event.getKeyCode();

	    keyEventMethods.handle(new Object[] { event });

	    switch (event.getID()) {
	    case KeyEvent.KEY_PRESSED:
	      keyPressed = true;
	      keyPressed();
	      break;
	    case KeyEvent.KEY_RELEASED:
	      keyPressed = false;
	      keyReleased();
	      break;
	    case KeyEvent.KEY_TYPED:
	      keyTyped();
	      break;
	    }

	    // if someone else wants to intercept the key, they should
	    // set key to zero (or something besides the ESC).
	    if (event.getID() == KeyEvent.KEY_PRESSED) {
	      if (key == KeyEvent.VK_ESCAPE) {
	        exit();
	      }
	      // When running tethered to the Processing application, respond to
	      // Ctrl-W (or Cmd-W) events by closing the sketch. Disable this behavior
	      // when running independently, because this sketch may be one component
	      // embedded inside an application that has its own close behavior.
	      if (external &&
	          event.getModifiers() == MENU_SHORTCUT &&
	          event.getKeyCode() == 'W') {
	        exit();
	      }
	    }
	  }


	  protected void checkKeyEvent(KeyEvent event) {
	    if (looping) {
	      enqueueKeyEvent(event);
	    } else {
	      handleKeyEvent(event);
	    }
	  }


	  /**
	   * Overriding keyXxxxx(KeyEvent e) functions will cause the 'key',
	   * 'keyCode', and 'keyEvent' variables to no longer work;
	   * key events will no longer be queued until the end of draw();
	   * and the keyPressed(), keyReleased() and keyTyped() methods
	   * will no longer be called.
	   */
	  public void keyPressed(KeyEvent e) { checkKeyEvent(e); }
	  public void keyReleased(KeyEvent e) { checkKeyEvent(e); }
	  public void keyTyped(KeyEvent e) { checkKeyEvent(e); }


	  /**
	   *
	   * The <b>keyPressed()</b> function is called once every time a key is pressed. The key that was pressed is stored in the <b>key</b> variable.
	   * <br><br>For non-ASCII keys, use the <b>keyCode</b> variable.
	   * The keys included in the ASCII specification (BACKSPACE, TAB, ENTER, RETURN, ESC, and DELETE) do not require checking to see if they key is coded, and you should simply use the <b>key</b> variable instead of <b>keyCode</b>
	   * If you're making cross-platform projects, note that the ENTER key is commonly used on PCs and Unix and the RETURN key is used instead on Macintosh.
	   * Check for both ENTER and RETURN to make sure your program will work for all platforms.<br><br>Because of how operating systems handle key repeats, holding down a key may cause multiple calls to keyPressed() (and keyReleased() as well).
	   * The rate of repeat is set by the operating system and how each computer is configured.
	   * =advanced
	   *
	   * Called each time a single key on the keyboard is pressed.
	   * Because of how operating systems handle key repeats, holding
	   * down a key will cause multiple calls to keyPressed(), because
	   * the OS repeat takes over.
	   * <P>
	   * Examples for key handling:
	   * (Tested on Windows XP, please notify if different on other
	   * platforms, I have a feeling Mac OS and Linux may do otherwise)
	   * <PRE>
	   * 1. Pressing 'a' on the keyboard:
	   *    keyPressed  with key == 'a' and keyCode == 'A'
	   *    keyTyped    with key == 'a' and keyCode ==  0
	   *    keyReleased with key == 'a' and keyCode == 'A'
	   *
	   * 2. Pressing 'A' on the keyboard:
	   *    keyPressed  with key == 'A' and keyCode == 'A'
	   *    keyTyped    with key == 'A' and keyCode ==  0
	   *    keyReleased with key == 'A' and keyCode == 'A'
	   *
	   * 3. Pressing 'shift', then 'a' on the keyboard (caps lock is off):
	   *    keyPressed  with key == CODED and keyCode == SHIFT
	   *    keyPressed  with key == 'A'   and keyCode == 'A'
	   *    keyTyped    with key == 'A'   and keyCode == 0
	   *    keyReleased with key == 'A'   and keyCode == 'A'
	   *    keyReleased with key == CODED and keyCode == SHIFT
	   *
	   * 4. Holding down the 'a' key.
	   *    The following will happen several times,
	   *    depending on your machine's "key repeat rate" settings:
	   *    keyPressed  with key == 'a' and keyCode == 'A'
	   *    keyTyped    with key == 'a' and keyCode ==  0
	   *    When you finally let go, you'll get:
	   *    keyReleased with key == 'a' and keyCode == 'A'
	   *
	   * 5. Pressing and releasing the 'shift' key
	   *    keyPressed  with key == CODED and keyCode == SHIFT
	   *    keyReleased with key == CODED and keyCode == SHIFT
	   *    (note there is no keyTyped)
	   *
	   * 6. Pressing the tab key in an applet with Java 1.4 will
	   *    normally do nothing, but PApplet dynamically shuts
	   *    this behavior off if Java 1.4 is in use (tested 1.4.2_05 Windows).
	   *    Java 1.1 (Microsoft VM) passes the TAB key through normally.
	   *    Not tested on other platforms or for 1.3.
	   * </PRE>
	   * @see PApplet#key
	   * @see PApplet#keyCode
	   * @see PApplet#keyPressed
	   * @see PApplet#keyReleased()
	   * @webref input:keyboard
	   */
	  public void keyPressed() { }


	  /**
	   * The <b>keyReleased()</b> function is called once every time a key is released. The key that was released will be stored in the <b>key</b> variable. See <b>key</b> and <b>keyReleased</b> for more information.
	   *
	   * @see PApplet#key
	   * @see PApplet#keyCode
	   * @see PApplet#keyPressed
	   * @see PApplet#keyPressed()
	   * @webref input:keyboard
	   */
	  public void keyReleased() { }


	  /**
	   * Only called for "regular" keys like letters,
	   * see keyPressed() for full documentation.
	   */
	  public void keyTyped() { }


	  //////////////////////////////////////////////////////////////

	  // i am focused man, and i'm not afraid of death.
	  // and i'm going all out. i circle the vultures in a van
	  // and i run the block.


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


	  /**
	   * The delay() function causes the program to halt for a specified time.
	   * Delay times are specified in thousandths of a second. For example,
	   * running delay(3000) will stop the program for three seconds and
	   * delay(500) will stop the program for a half-second. Remember: the
	   * display window is updated only at the end of draw(), so putting more
	   * than one delay() inside draw() will simply add them together and the new
	   * frame will be drawn when the total delay is over.
	   * <br/> <br/>
	   * I'm not sure if this is even helpful anymore, as the screen isn't
	   * updated before or after the delay, meaning which means it just
	   * makes the app lock up temporarily.
	   */
	  public void delay(int napTime) {
	    if (frameCount != 0) {
	      if (napTime > 0) {
	        try {
	          Thread.sleep(napTime);
	        } catch (InterruptedException e) { }
	      }
	    }
	  }


	  /**
	   * Specifies the number of frames to be displayed every second.
	   * If the processor is not fast enough to maintain the specified rate, it will not be achieved.
	   * For example, the function call <b>frameRate(30)</b> will attempt to refresh 30 times a second.
	   * It is recommended to set the frame rate within <b>setup()</b>. The default rate is 60 frames per second.
	   *  =advanced
	   * Set a target frameRate. This will cause delay() to be called
	   * after each frame so that the sketch synchronizes to a particular speed.
	   * Note that this only sets the maximum frame rate, it cannot be used to
	   * make a slow sketch go faster. Sketches have no default frame rate
	   * setting, and will attempt to use maximum processor power to achieve
	   * maximum speed.
	   * @webref environment
	   * @param newRateTarget number of frames per second
	   * @see PApplet#delay(int)
	   */
	  public void frameRate(float newRateTarget) {
	    frameRateTarget = newRateTarget;
	    frameRatePeriod = (long) (1000000000.0 / frameRateTarget);
	  }

	  

	  static String openLauncher;

	  /**
	   * Launch a process using a platforms shell. This version uses an array
	   * to make it easier to deal with spaces in the individual elements.
	   * (This avoids the situation of trying to put single or double quotes
	   * around different bits).
	   *
	   * @param list of commands passed to the command line
	   */
	  static public Process open(String argv[]) {
	    String[] params = null;

	    if (platform == WINDOWS) {
	      // just launching the .html file via the shell works
	      // but make sure to chmod +x the .html files first
	      // also place quotes around it in case there's a space
	      // in the user.dir part of the url
	      params = new String[] { "cmd", "/c" };

	    } else if (platform == MACOSX) {
	      params = new String[] { "open" };

	    } else if (platform == LINUX) {
	      if (openLauncher == null) {
	        // Attempt to use gnome-open
	        try {
	          Process p = Runtime.getRuntime().exec(new String[] { "gnome-open" });
	          /*int result =*/ p.waitFor();
	          // Not installed will throw an IOException (JDK 1.4.2, Ubuntu 7.04)
	          openLauncher = "gnome-open";
	        } catch (Exception e) { }
	      }
	      if (openLauncher == null) {
	        // Attempt with kde-open
	        try {
	          Process p = Runtime.getRuntime().exec(new String[] { "kde-open" });
	          /*int result =*/ p.waitFor();
	          openLauncher = "kde-open";
	        } catch (Exception e) { }
	      }
	      if (openLauncher == null) {
	        System.err.println("Could not find gnome-open or kde-open, " +
	                           "the open() command may not work.");
	      }
	      if (openLauncher != null) {
	        params = new String[] { openLauncher };
	      }
	    //} else {  // give up and just pass it to Runtime.exec()
	      //open(new String[] { filename });
	      //params = new String[] { filename };
	    }
	    if (params != null) {
	      // If the 'open', 'gnome-open' or 'cmd' are already included
	      if (params[0].equals(argv[0])) {
	        // then don't prepend those params again
	        return exec(argv);
	      } else {
	        params = concat(params, argv);
	        return exec(params);
	      }
	    } else {
	      return exec(argv);
	    }
	  }


	  static public Process exec(String[] argv) {
	    try {
	      return Runtime.getRuntime().exec(argv);
	    } catch (Exception e) {
	      e.printStackTrace();
	      throw new RuntimeException("Could not open " + join(argv, ' '));
	    }
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
	      exit2();

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
	      exit2();
	    }
	  }


	  void exit2() {
	    try {
	      System.exit(0);
	    } catch (SecurityException e) {
	      // don't care about applet security exceptions
	    }
	  }



	  //////////////////////////////////////////////////////////////


	  public void method(String name) {
//	    final Object o = this;
//	    final Class<?> c = getClass();
	    try {
	      Method method = getClass().getMethod(name, new Class[] {});
	      method.invoke(this, new Object[] { });

	    } catch (IllegalArgumentException e) {
	      e.printStackTrace();
	    } catch (IllegalAccessException e) {
	      e.printStackTrace();
	    } catch (InvocationTargetException e) {
	      e.getTargetException().printStackTrace();
	    } catch (NoSuchMethodException nsme) {
	      System.err.println("There is no public " + name + "() method " +
	                         "in the class " + getClass().getName());
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }


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

	  //


	  int cursorType = ARROW; // cursor type
	  boolean cursorVisible = true; // cursor visibility flag
	  PImage invisibleCursor;


	  /**
	   * Set the cursor type
	   * @param cursorType either ARROW, CROSS, HAND, MOVE, TEXT, WAIT
	   */
	  public void cursor(int cursorType) {
	    setCursor(Cursor.getPredefinedCursor(cursorType));
	    cursorVisible = true;
	    this.cursorType = cursorType;
	  }


	  /**
	   * Replace the cursor with the specified PImage. The x- and y-
	   * coordinate of the center will be the center of the image.
	   */
	  public void cursor(PImage image) {
	    cursor(image, image.width/2, image.height/2);
	  }


	  /**
	   * Sets the cursor to a predefined symbol, an image, or turns it on if already hidden.
	   * If you are trying to set an image as the cursor, it is recommended to make the size 16x16 or 32x32 pixels.
	   * It is not possible to load an image as the cursor if you are exporting your program for the Web.
	   * The values for parameters <b>x</b> and <b>y</b> must be less than the dimensions of the image.
	   * =advanced
	   * Set a custom cursor to an image with a specific hotspot.
	   * Only works with JDK 1.2 and later.
	   * Currently seems to be broken on Java 1.4 for Mac OS X
	   * <P>
	   * Based on code contributed by Amit Pitaru, plus additional
	   * code to handle Java versions via reflection by Jonathan Feinberg.
	   * Reflection removed for release 0128 and later.
	   * @webref environment
	   * @see       PApplet#noCursor()
	   * @param image       any variable of type PImage
	   * @param hotspotX    the horizonal active spot of the cursor
	   * @param hotspotY    the vertical active spot of the cursor
	   */
	  public void cursor(PImage image, int hotspotX, int hotspotY) {
	    // don't set this as cursor type, instead use cursor_type
	    // to save the last cursor used in case cursor() is called
	    //cursor_type = Cursor.CUSTOM_CURSOR;
	    Image jimage =
	      createImage(new MemoryImageSource(image.width, image.height,
	                                        image.pixels, 0, image.width));
	    Point hotspot = new Point(hotspotX, hotspotY);
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    Cursor cursor = tk.createCustomCursor(jimage, hotspot, "Custom Cursor");
	    setCursor(cursor);
	    cursorVisible = true;
	  }


	  /**
	   * Show the cursor after noCursor() was called.
	   * Notice that the program remembers the last set cursor type
	   */
	  public void cursor() {
	    // maybe should always set here? seems dangerous, since
	    // it's likely that java will set the cursor to something
	    // else on its own, and the applet will be stuck b/c bagel
	    // thinks that the cursor is set to one particular thing
	    if (!cursorVisible) {
	      cursorVisible = true;
	      setCursor(Cursor.getPredefinedCursor(cursorType));
	    }
	  }


	  /**
	   * Hides the cursor from view. Will not work when running the program in a web browser.
	   * =advanced
	   * Hide the cursor by creating a transparent image
	   * and using it as a custom cursor.
	   * @webref environment
	   * @see PApplet#cursor()
	   * @usage Application
	   */
	  public void noCursor() {
	    if (!cursorVisible) return;  // don't hide if already hidden.

	    if (invisibleCursor == null) {
	      invisibleCursor = new PImage(16, 16, ARGB);
	    }
	    // was formerly 16x16, but the 0x0 was added by jdf as a fix
	    // for macosx, which wasn't honoring the invisible cursor
	    cursor(invisibleCursor, 8, 8);
	    cursorVisible = false;
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


	  /**
	   * GIF image of the Processing logo.
	   */
	  static public final byte[] ICON_IMAGE = {
	    71, 73, 70, 56, 57, 97, 16, 0, 16, 0, -77, 0, 0, 0, 0, 0, -1, -1, -1, 12,
	    12, 13, -15, -15, -14, 45, 57, 74, 54, 80, 111, 47, 71, 97, 62, 88, 117,
	    1, 14, 27, 7, 41, 73, 15, 52, 85, 2, 31, 55, 4, 54, 94, 18, 69, 109, 37,
	    87, 126, -1, -1, -1, 33, -7, 4, 1, 0, 0, 15, 0, 44, 0, 0, 0, 0, 16, 0, 16,
	    0, 0, 4, 122, -16, -107, 114, -86, -67, 83, 30, -42, 26, -17, -100, -45,
	    56, -57, -108, 48, 40, 122, -90, 104, 67, -91, -51, 32, -53, 77, -78, -100,
	    47, -86, 12, 76, -110, -20, -74, -101, 97, -93, 27, 40, 20, -65, 65, 48,
	    -111, 99, -20, -112, -117, -123, -47, -105, 24, 114, -112, 74, 69, 84, 25,
	    93, 88, -75, 9, 46, 2, 49, 88, -116, -67, 7, -19, -83, 60, 38, 3, -34, 2,
	    66, -95, 27, -98, 13, 4, -17, 55, 33, 109, 11, 11, -2, -128, 121, 123, 62,
	    91, 120, -128, 127, 122, 115, 102, 2, 119, 0, -116, -113, -119, 6, 102,
	    121, -108, -126, 5, 18, 6, 4, -102, -101, -100, 114, 15, 17, 0, 59
	  };


	  /**
	   * main() method for running this class from the command line.
	   * <P>
	   * <B>The options shown here are not yet finalized and will be
	   * changing over the next several releases.</B>
	   * <P>
	   * The simplest way to turn and applet into an application is to
	   * add the following code to your program:
	   * <PRE>static public void main(String args[]) {
	   *   PApplet.main(new String[] { "YourSketchName" });
	   * }</PRE>
	   * This will properly launch your applet from a double-clickable
	   * .jar or from the command line.
	   * <PRE>
	   * Parameters useful for launching or also used by the PDE:
	   *
	   * --location=x,y        upper-lefthand corner of where the applet
	   *                       should appear on screen. if not used,
	   *                       the default is to center on the main screen.
	   *
	   * --present             put the applet into full screen presentation
	   *                       mode. requires java 1.4 or later.
	   *
	   * --exclusive           use full screen exclusive mode when presenting.
	   *                       disables new windows or interaction with other
	   *                       monitors, this is like a "game" mode.
	   *
	   * --hide-stop           use to hide the stop button in situations where
	   *                       you don't want to allow users to exit. also
	   *                       see the FAQ on information for capturing the ESC
	   *                       key when running in presentation mode.
	   *
	   * --stop-color=#xxxxxx  color of the 'stop' text used to quit an
	   *                       sketch when it's in present mode.
	   *
	   * --bgcolor=#xxxxxx     background color of the window.
	   *
	   * --sketch-path         location of where to save files from functions
	   *                       like saveStrings() or saveFrame(). defaults to
	   *                       the folder that the java application was
	   *                       launched from, which means if this isn't set by
	   *                       the pde, everything goes into the same folder
	   *                       as processing.exe.
	   *
	   * --display=n           set what display should be used by this applet.
	   *                       displays are numbered starting from 1.
	   *
	   * Parameters used by Processing when running via the PDE
	   *
	   * --external            set when the applet is being used by the PDE
	   *
	   * --editor-location=x,y position of the upper-lefthand corner of the
	   *                       editor window, for placement of applet window
	   * </PRE>
	   */
//	  static public void main(String args[]) {
//	    // Disable abyssmally slow Sun renderer on OS X 10.5.
//	    if (platform == MACOSX) {
//	      // Only run this on OS X otherwise it can cause a permissions error.
//	      // http://dev.processing.org/bugs/show_bug.cgi?id=976
//	      System.setProperty("apple.awt.graphics.UseQuartz", 
//	                         String.valueOf(useQuartz));
//	    }
//
//	    // This doesn't do anything.
////	    if (platform == WINDOWS) {
////	      // For now, disable the D3D renderer on Java 6u10 because
////	      // it causes problems with Present mode.
////	      // http://dev.processing.org/bugs/show_bug.cgi?id=1009
////	      System.setProperty("sun.java2d.d3d", "false");
////	    }
//
//	    if (args.length < 1) {
//	      System.err.println("Usage: PApplet <appletname>");
//	      System.err.println("For additional options, " +
//	                         "see the Javadoc for PApplet");
//	      System.exit(1);
//	    }
//
//	    boolean external = false;
//	    int[] location = null;
//	    int[] editorLocation = null;
//
//	    String name = null;
//	    boolean present = false;
//	    boolean exclusive = false;
//	    Color backgroundColor = Color.BLACK;
//	    Color stopColor = Color.GRAY;
//	    GraphicsDevice displayDevice = null;
//	    boolean hideStop = false;
//
//	    String param = null, value = null;
//
//	    // try to get the user folder. if running under java web start,
//	    // this may cause a security exception if the code is not signed.
//	    // http://processing.org/discourse/yabb_beta/YaBB.cgi?board=Integrate;action=display;num=1159386274
//	    String folder = null;
//	    try {
//	      folder = System.getProperty("user.dir");
//	    } catch (Exception e) { }
//
//	    int argIndex = 0;
//	    while (argIndex < args.length) {
//	      int equals = args[argIndex].indexOf('=');
//	      if (equals != -1) {
//	        param = args[argIndex].substring(0, equals);
//	        value = args[argIndex].substring(equals + 1);
//
//	        if (param.equals(ARGS_EDITOR_LOCATION)) {
//	          external = true;
//	          editorLocation = parseInt(split(value, ','));
//
//	        } else if (param.equals(ARGS_DISPLAY)) {
//	          int deviceIndex = Integer.parseInt(value) - 1;
//
//	          //DisplayMode dm = device.getDisplayMode();
//	          //if ((dm.getWidth() == 1024) && (dm.getHeight() == 768)) {
//
//	          GraphicsEnvironment environment =
//	            GraphicsEnvironment.getLocalGraphicsEnvironment();
//	          GraphicsDevice devices[] = environment.getScreenDevices();
//	          if ((deviceIndex >= 0) && (deviceIndex < devices.length)) {
//	            displayDevice = devices[deviceIndex];
//	          } else {
//	            System.err.println("Display " + value + " does not exist, " +
//	                               "using the default display instead.");
//	          }
//
//	        } else if (param.equals(ARGS_BGCOLOR)) {
//	          if (value.charAt(0) == '#') value = value.substring(1);
//	          backgroundColor = new Color(Integer.parseInt(value, 16));
//
//	        } else if (param.equals(ARGS_STOP_COLOR)) {
//	          if (value.charAt(0) == '#') value = value.substring(1);
//	          stopColor = new Color(Integer.parseInt(value, 16));
//
//	        } else if (param.equals(ARGS_SKETCH_FOLDER)) {
//	          folder = value;
//
//	        } else if (param.equals(ARGS_LOCATION)) {
//	          location = parseInt(split(value, ','));
//	        }
//
//	      } else {
//	        if (args[argIndex].equals(ARGS_PRESENT)) {
//	          present = true;
//
//	        } else if (args[argIndex].equals(ARGS_EXCLUSIVE)) {
//	          exclusive = true;
//
//	        } else if (args[argIndex].equals(ARGS_HIDE_STOP)) {
//	          hideStop = true;
//
//	        } else if (args[argIndex].equals(ARGS_EXTERNAL)) {
//	          external = true;
//
//	        } else {
//	          name = args[argIndex];
//	          break;
//	        }
//	      }
//	      argIndex++;
//	    }
//
//	    // Set this property before getting into any GUI init code
//	    //System.setProperty("com.apple.mrj.application.apple.menu.about.name", name);
//	    // This )*)(*@#$ Apple crap don't work no matter where you put it
//	    // (static method of the class, at the top of main, wherever)
//
//	    if (displayDevice == null) {
//	      GraphicsEnvironment environment =
//	        GraphicsEnvironment.getLocalGraphicsEnvironment();
//	      displayDevice = environment.getDefaultScreenDevice();
//	    }
//
//	    Frame frame = new Frame(displayDevice.getDefaultConfiguration());
//	      /*
//	      Frame frame = null;
//	      if (displayDevice != null) {
//	        frame = new Frame(displayDevice.getDefaultConfiguration());
//	      } else {
//	        frame = new Frame();
//	      }
//	      */
//	      //Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
//
//	    // remove the grow box by default
//	    // users who want it back can call frame.setResizable(true)
//	    frame.setResizable(false);
//
//	    // Set the trimmings around the image
//	    Image image = Toolkit.getDefaultToolkit().createImage(ICON_IMAGE);
//	    frame.setIconImage(image);
//	    frame.setTitle(name);
//
//	    final PApplet applet;
//	    try {
//	      Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(name);
//	      applet = (PApplet) c.newInstance();
//	    } catch (Exception e) {
//	      throw new RuntimeException(e);
//	    }
//
//	    // these are needed before init/start
//	    applet.frame = frame;
//	    applet.sketchPath = folder;
//	    applet.args = PApplet.subset(args, 1);
//	    applet.external = external;
//
//	    // Need to save the window bounds at full screen,
//	    // because pack() will cause the bounds to go to zero.
//	    // http://dev.processing.org/bugs/show_bug.cgi?id=923
//	    Rectangle fullScreenRect = null;
//
//	    // For 0149, moving this code (up to the pack() method) before init().
//	    // For OpenGL (and perhaps other renderers in the future), a peer is
//	    // needed before a GLDrawable can be created. So pack() needs to be
//	    // called on the Frame before applet.init(), which itself calls size(),
//	    // and launches the Thread that will kick off setup().
//	    // http://dev.processing.org/bugs/show_bug.cgi?id=891
//	    // http://dev.processing.org/bugs/show_bug.cgi?id=908
//	    if (present) {
//	      frame.setUndecorated(true);
//	      frame.setBackground(backgroundColor);
//	      if (exclusive) {
//	        displayDevice.setFullScreenWindow(frame);
//	        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
//	        fullScreenRect = frame.getBounds();
//	      } else {
//	        DisplayMode mode = displayDevice.getDisplayMode();
//	        fullScreenRect = new Rectangle(0, 0, mode.getWidth(), mode.getHeight());
//	        frame.setBounds(fullScreenRect);
//	        frame.setVisible(true);
//	      }
//	    }
//	    frame.setLayout(null);
//	    frame.add(applet);
//	    if (present) {
//	      frame.invalidate();
//	    } else {
//	      frame.pack();
//	    }
//	    // insufficient, places the 100x100 sketches offset strangely
//	    //frame.validate();
//
//	    applet.init();
//
//	    // Wait until the applet has figured out its width.
//	    // In a static mode app, this will be after setup() has completed,
//	    // and the empty draw() has set "finished" to true.
//	    // TODO make sure this won't hang if the applet has an exception.
//	    while (applet.defaultSize && !applet.finished) {
//	      //System.out.println("default size");
//	      try {
//	        Thread.sleep(5);
//
//	      } catch (InterruptedException e) {
//	        //System.out.println("interrupt");
//	      }
//	    }
//	    //println("not default size " + applet.width + " " + applet.height);
//	    //println("  (g width/height is " + applet.g.width + "x" + applet.g.height + ")");
//
//	    if (present) {
//	      // After the pack(), the screen bounds are gonna be 0s
//	      frame.setBounds(fullScreenRect);
//	      applet.setBounds((fullScreenRect.width - applet.width) / 2,
//	                       (fullScreenRect.height - applet.height) / 2,
//	                       applet.width, applet.height);
//
//	      if (!hideStop) {
//	        Label label = new Label("stop");
//	        label.setForeground(stopColor);
//	        label.addMouseListener(new MouseAdapter() {
//	            public void mousePressed(MouseEvent e) {
//	              System.exit(0);
//	            }
//	          });
//	        frame.add(label);
//
//	        Dimension labelSize = label.getPreferredSize();
//	        // sometimes shows up truncated on mac
//	        //System.out.println("label width is " + labelSize.width);
//	        labelSize = new Dimension(100, labelSize.height);
//	        label.setSize(labelSize);
//	        label.setLocation(20, fullScreenRect.height - labelSize.height - 20);
//	      }
//
//	      // not always running externally when in present mode
//	      if (external) {
//	        applet.setupExternalMessages();
//	      }
//
//	    } else {  // if not presenting
//	      // can't do pack earlier cuz present mode don't like it
//	      // (can't go full screen with a frame after calling pack)
//	      //        frame.pack();  // get insets. get more.
//	      Insets insets = frame.getInsets();
//
//	      int windowW = Math.max(applet.width, MIN_WINDOW_WIDTH) +
//	        insets.left + insets.right;
//	      int windowH = Math.max(applet.height, MIN_WINDOW_HEIGHT) +
//	        insets.top + insets.bottom;
//
//	      frame.setSize(windowW, windowH);
//
//	      if (location != null) {
//	        // a specific location was received from PdeRuntime
//	        // (applet has been run more than once, user placed window)
//	        frame.setLocation(location[0], location[1]);
//
//	      } else if (external) {
//	        int locationX = editorLocation[0] - 20;
//	        int locationY = editorLocation[1];
//
//	        if (locationX - windowW > 10) {
//	          // if it fits to the left of the window
//	          frame.setLocation(locationX - windowW, locationY);
//
//	        } else {  // doesn't fit
//	          // if it fits inside the editor window,
//	          // offset slightly from upper lefthand corner
//	          // so that it's plunked inside the text area
//	          locationX = editorLocation[0] + 66;
//	          locationY = editorLocation[1] + 66;
//
//	          if ((locationX + windowW > applet.screenWidth - 33) ||
//	              (locationY + windowH > applet.screenHeight - 33)) {
//	            // otherwise center on screen
//	            locationX = (applet.screenWidth - windowW) / 2;
//	            locationY = (applet.screenHeight - windowH) / 2;
//	          }
//	          frame.setLocation(locationX, locationY);
//	        }
//	      } else {  // just center on screen
//	        frame.setLocation((applet.screenWidth - applet.width) / 2,
//	                          (applet.screenHeight - applet.height) / 2);
//	      }
//	      Point frameLoc = frame.getLocation();
//	      if (frameLoc.y < 0) {
//	        // Windows actually allows you to place frames where they can't be 
//	        // closed. Awesome. http://dev.processing.org/bugs/show_bug.cgi?id=1508
//	        frame.setLocation(frameLoc.x, 30);
//	      }
//
//	      if (backgroundColor == Color.black) {  //BLACK) {
//	        // this means no bg color unless specified
//	        backgroundColor = SystemColor.control;
//	      }
//	      frame.setBackground(backgroundColor);
//
//	      int usableWindowH = windowH - insets.top - insets.bottom;
//	      applet.setBounds((windowW - applet.width)/2,
//	                       insets.top + (usableWindowH - applet.height)/2,
//	                       applet.width, applet.height);
//
//	      if (external) {
//	        applet.setupExternalMessages();
//
//	      } else {  // !external
//	        frame.addWindowListener(new WindowAdapter() {
//	            public void windowClosing(WindowEvent e) {
//	              System.exit(0);
//	            }
//	          });
//	      }
//
//	      // handle frame resizing events
//	      applet.setupFrameResizeListener();
//
//	      // all set for rockin
//	      if (applet.displayable()) {
//	        frame.setVisible(true);
//	      }
//	    }
//
//	    applet.requestFocus(); // ask for keydowns
//	    //System.out.println("exiting main()");
//	  }


}