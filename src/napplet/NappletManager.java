package napplet;

import static java.awt.event.FocusEvent.FOCUS_GAINED;
import static java.awt.event.FocusEvent.FOCUS_LOST;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class NAppletManager {

	List<NApplet> nAppletList = new ArrayList<NApplet>();
	List<NApplet> killList = new ArrayList<NApplet>();
	
	PApplet parentPApplet;

	NApplet focusNApplet;
	NApplet mouseNApplet;
	public int mouseX, mouseY;

	public NAppletManager(PApplet pap) {
		super();
		parentPApplet = pap;
		parentPApplet.registerPre(this);
		parentPApplet.registerDraw(this);
		parentPApplet.registerMouseEvent(this);
		parentPApplet.registerKeyEvent(this);
	}

	public void addNApplet(NApplet nap) {
		nap.parentPApplet = parentPApplet;
		nAppletList.add(nap);
		nap.setup();
	}

	public NApplet containingNapplet(int x, int y) {
		NApplet containingNApplet = null;
		for (NApplet nap : nAppletList) {
			if (nap.embeddedNApplet) { 
				int xRel = x - nap.nappletX;
				int yRel = y - nap.nappletY;
				if (xRel >= 0 && yRel >= 0 && xRel < nap.width && yRel < nap.height) {
					containingNApplet = nap;
				}
			}
		}
		return containingNApplet;
	}

	public void pre() {

	}
	
	public void draw() {
		for (NApplet nap : nAppletList) {
			nap.runFrame();
		}
		while (killList.size()>0) {
			nAppletList.remove(killList.get(0));
			killList.remove(0);
		}
	}

	void passMouseEvent(NApplet nap, MouseEvent event) {
		event.translatePoint(-(nap.nappletX), -(nap.nappletY));
		nap.passMouseEvent(event);
	}

	public void mouseEvent(MouseEvent event) {
		mouseX = event.getX();
		mouseY = event.getY();

		NApplet nap = containingNapplet(mouseX, mouseY);
		if ((event.getID() == java.awt.event.MouseEvent.MOUSE_DRAGGED)
				&& (mouseNApplet != null)) {
			passMouseEvent(mouseNApplet, event);
		} else if (nap != null) {
			passMouseEvent(nap, event);
			mouseNApplet = nap;
			if (nap != focusNApplet) {
				FocusEvent gainFocus = new FocusEvent(nap, FOCUS_GAINED, false,
						focusNApplet);
				nap.focusGained(gainFocus);
				if (focusNApplet!=null) {	
					FocusEvent loseFocus = new FocusEvent(focusNApplet,
							FOCUS_LOST, false, nap);
					focusNApplet.focusLost(loseFocus);
				}
				focusNApplet = nap;
			}
		}
	}

	public void keyEvent(KeyEvent event) {
		if (focusNApplet != null) {
			focusNApplet.passKeyEvent(event);
		}
	}
	
	public void createNApplet(String nappletClassName, int x, int y) {
		createEmbeddedNApplet(nappletClassName, x, y);
	}
	
	public void createEmbeddedNApplet(String nappletClassName, int x, int y) {
		NApplet nap = NApplet.createNApplet(parentPApplet, nappletClassName, this);
		if (nap!=null) {
			nap.initEmbeddedNApplet(parentPApplet, x, y);
			addNApplet(nap);
		}
	}
	
	public void createWindowedNApplet(String nappletClassName, int x, int y) {
		NApplet nap = NApplet.createNApplet(parentPApplet, nappletClassName, this);
		if (nap!=null) {
			@SuppressWarnings("unused")
			NFrame nFrame = new NFrame(parentPApplet, nap, x, y);
			addNApplet(nap);
		}
	}
	
	public void killNApplet(NApplet napplet) {
		killList.add(napplet);
	}
	
}
