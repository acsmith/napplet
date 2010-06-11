package napplet;

import static java.awt.event.FocusEvent.FOCUS_GAINED;
import static java.awt.event.FocusEvent.FOCUS_LOST;
import static java.awt.event.MouseEvent.MOUSE_DRAGGED;
import static java.awt.event.MouseEvent.MOUSE_MOVED;
import static java.awt.event.MouseEvent.MOUSE_RELEASED;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class NAppletManager implements MouseListener, MouseMotionListener,
		MouseWheelListener, KeyListener, ComponentListener {

	public boolean keyPassthroughPolicy = false;
	public boolean mousePassthroughPolicy = false;

	List<Nit> nitList = new ArrayList<Nit>();
	List<Nit> killList = new ArrayList<Nit>();

	PApplet parentPApplet;

	Nit focusNit;
	Nit mouseNit;
	public int mouseX, mouseY;

	public boolean componentListenerInitialized = false;

	public boolean resizeModeChangeRequested = false;
	public boolean resizeModeRequested;

	public boolean resizeRequested = false;
	public int resizeWidth;
	public int resizeHeight;

	public NAppletManager(PApplet pap) {
		super();
		parentPApplet = pap;

		if (parentPApplet instanceof NApplet) {
			((NApplet) parentPApplet).nappletManager = this;
			componentListenerInitialized = true;
		} else {
			parentPApplet.addMouseWheelListener(this);
		}

		parentPApplet.registerPre(this);
		parentPApplet.registerDraw(this);
		parentPApplet.registerPost(this);

		parentPApplet.removeMouseListener(parentPApplet);
		parentPApplet.addMouseListener(this);
		parentPApplet.removeMouseMotionListener(parentPApplet);
		parentPApplet.addMouseMotionListener(this);

		parentPApplet.removeKeyListener(parentPApplet);
		parentPApplet.addKeyListener(this);
	}

	public void addNit(Nit nit) {
		nit.setNAppletManager(this);
		nitList.add(nit);
	}

	public void addNApplet(NApplet napplet) {
		addNit(napplet);
	}

	public Nit containingNit(int x, int y) {
		Nit containingNit = null;
		for (Nit nit : nitList) {
			if (nit.isEmbedded()) {
				int xRel = x - nit.getPositionX();
				int yRel = y - nit.getPositionY();
				if (xRel >= 0 && yRel >= 0 && xRel < nit.getWidth()
						&& yRel < nit.getHeight()) {
					containingNit = nit;
				}
			}
		}
		return containingNit;
	}

	public void pre() {

	}

	public void draw() {
		parentPApplet.loadPixels();
		for (Nit nit : nitList) {
			boolean translated = false;
			if (nit.isEmbedded()) {
				parentPApplet.pushMatrix();
				parentPApplet.translate(nit.getPositionX(), nit.getPositionY());
				translated = true;
			}
			nit.runFrame();
			if (translated)
				parentPApplet.popMatrix();

			if (nit instanceof NApplet && !(nit.isEmbedded())
					&& nit.getFrame() == null) {
				@SuppressWarnings("unused")
				NFrame nFrame = new NFrame(parentPApplet, (NApplet) nit, nit
						.getPositionX(), nit.getPositionY());
			}
		}
		
		parentPApplet.updatePixels();
		while (killList.size() > 0) {
			nitList.remove(killList.get(0));
			killList.remove(0);
		}
	}

	public void post() {
		if (resizeModeChangeRequested && parentPApplet.frame != null) {
			parentPApplet.frame.setResizable(resizeModeRequested);
			resizeModeChangeRequested = false;
		}

		for (Nit n : nitList) {
			if (n instanceof NApplet) {
				NApplet nap = (NApplet) n;
				if (nap.resizeRequest) {
					nap.resizeRenderer(nap.resizeWidth, nap.resizeHeight);
					nap.resizeRequest = false;
				}
			}
		}

		if (!(componentListenerInitialized)
				&& (parentPApplet.getParent() != null)) {
			parentPApplet.getParent().addComponentListener(this);
			componentListenerInitialized = true;

		}
	}

	void passMouseEvent(Nit nit, MouseEvent e) {
		MouseEvent ep = new MouseEvent((Component) (e.getSource()), e.getID(),
				e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e
						.getClickCount(), e.isPopupTrigger(), e.getButton());
		ep.translatePoint(-(nit.getPositionX()), -(nit.getPositionY()));
		nit.passEvent(ep);
	}

	public void handleMouseEvent(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		int id = e.getID();
		Nit nit = containingNit(mouseX, mouseY);

		if (nit != focusNit) {
			NApplet focusGainer = (nit instanceof NApplet) ? (NApplet) nit
					: null;
			NApplet focusLoser = (focusNit instanceof NApplet) ? (NApplet) focusNit
					: null;
			if (nit instanceof NApplet)
				nit.focusGained(new FocusEvent(focusGainer, FOCUS_GAINED,
						false, focusLoser));
			if (focusNit instanceof NApplet)
				focusNit.focusLost(new FocusEvent(focusLoser, FOCUS_LOST,
						false, focusGainer));
			focusNit = nit;
		}

		if (mouseNit != null && id == MOUSE_RELEASED) {
			passMouseEvent(mouseNit, e);
			mouseNit = nit;
		}
		if (mouseNit == null)
			mouseNit = nit;

		if (nit != null) {
			if (id == MOUSE_DRAGGED) {
				MouseEvent moveEvent = new MouseEvent(
						(Component) e.getSource(), MOUSE_MOVED, e.getWhen(), e
								.getModifiers(), e.getX(), e.getY(), e
								.getClickCount(), e.isPopupTrigger(), e
								.getButton());
				parentPApplet.mouseMoved(moveEvent);

			}
			passMouseEvent(nit, e);
		}

		if ((nit == null) || (!e.isConsumed() && mousePassthroughPolicy)) {
			parentPApplet.mousePressed(e);
		}
	}

	public void handleMouseWheelEvent(MouseWheelEvent e) {
		if (focusNit != null) {
			focusNit.passEvent(e);
		}
		if (focusNit == null || (!e.isConsumed() && mousePassthroughPolicy)) {
			if (parentPApplet instanceof NApplet) {
				((NApplet) parentPApplet).mouseWheelMoved(e);
			}
		}
	}

	public void handleKeyEvent(KeyEvent e) {
		if (focusNit != null) {
			focusNit.passEvent(e);
		}
		if (focusNit == null || (!e.isConsumed() && keyPassthroughPolicy)) {
			parentPApplet.keyPressed(e);
		}
	}

	public NApplet createNApplet(String nappletClassName, int x, int y) {
		return createEmbeddedNApplet(nappletClassName, x, y);
	}

	public void addEmbeddedNApplet(NApplet nap, int x, int y) {
		nap.initEmbeddedNApplet(parentPApplet, x, y);
		addNit(nap);
		if (nap.g.format == processing.core.PConstants.RGB)
			nap.g.format = processing.core.PConstants.ARGB;
	}

	public void addWindowedNApplet(NApplet nap, int x, int y) {
		nap.initWindowedNApplet(parentPApplet, x, y,
				parentPApplet.sketchPath);
		addNit(nap);
	}
	
	public NApplet createEmbeddedNApplet(String nappletClassName, int x, int y) {
		NApplet nap = NApplet.createNApplet(parentPApplet, nappletClassName,
				this);
		if (nap != null)
			addEmbeddedNApplet(nap, x, y);
		return nap;
	}

	public NApplet createWindowedNApplet(String nappletClassName, int x, int y) {
		NApplet nap = NApplet.createNApplet(parentPApplet, nappletClassName,
				this);
		if (nap != null)
			addWindowedNApplet(nap, x, y);
		return nap;
	}

	public void setResizable(boolean resizable) {
		resizeModeChangeRequested = true;
		resizeModeRequested = resizable;
	}

	public void killNit(Nit nit) {
		killList.add(nit);
	}

	public void killNApplet(NApplet napplet) {
		killNit(napplet);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		handleMouseWheelEvent(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		handleMouseEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		handleMouseEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		handleMouseEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		handleMouseEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		handleMouseEvent(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		handleMouseEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		handleMouseEvent(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		handleKeyEvent(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		handleKeyEvent(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		handleKeyEvent(e);
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

}
