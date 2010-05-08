package napplet;

import static java.awt.event.FocusEvent.FOCUS_GAINED;
import static java.awt.event.FocusEvent.FOCUS_LOST;
import static processing.core.PConstants.ARGB;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class NibbleManager {

	List<Nibble> nibbleList = new ArrayList<Nibble>();
	List<NApplet> killList = new ArrayList<NApplet>();

	PApplet parentPApplet;

	Nibble focusNibble;
	Nibble mouseNibble;
	public int mouseX, mouseY;

	public NibbleManager(PApplet pap) {
		super();
		parentPApplet = pap;
		parentPApplet.registerPre(this);
		parentPApplet.registerDraw(this);
		parentPApplet.registerMouseEvent(this);
		parentPApplet.registerKeyEvent(this);
	}

	public void addNibble(Nibble nib) {
		nib.setParentPApplet(parentPApplet);
		nibbleList.add(nib);
		nib.setup();
	}

	public Nibble containingNibble(int x, int y) {
		Nibble containingNibble = null;
		for (Nibble nib : nibbleList) {
			if (nib.isEmbedded()) {
				int xRel = x - nib.getPositionX();
				int yRel = y - nib.getPositionY();
				if (xRel >= 0 && yRel >= 0 && xRel < nib.getWidth()
						&& yRel < nib.getHeight()) {
					containingNibble = nib;
				}
			}
		}
		return containingNibble;
	}

	public void pre() {

	}

	public void draw() {
		for (Nibble nib : nibbleList) {
			nib.runFrame();
		}
		while (killList.size() > 0) {
			nibbleList.remove(killList.get(0));
			killList.remove(0);
		}
	}

	void passMouseEvent(NApplet nap, MouseEvent event) {
		event.translatePoint(-(nap.getPositionX()), -(nap.getPositionY()));
		nap.passMouseEvent(event);
	}

	public void mouseEvent(MouseEvent event) {
		mouseX = event.getX();
		mouseY = event.getY();

		Nibble nib = containingNibble(mouseX, mouseY);
		if ((event.getID() == java.awt.event.MouseEvent.MOUSE_DRAGGED)
				&& (mouseNibble != null)) {
			passMouseEvent((NApplet) mouseNibble, event);
		} else if (nib != null) {
			passMouseEvent((NApplet) nib, event);
			mouseNibble = nib;
			if (nib != focusNibble) {
				Nibble focusGainingNApplet = (nib instanceof NApplet) ? nib
						: null;
				Nibble focusLosingNApplet = (focusNibble instanceof NApplet) ? focusNibble
						: null;
				if (focusGainingNApplet != null) {
					FocusEvent gainFocus = new FocusEvent(
							(NApplet) focusGainingNApplet, FOCUS_GAINED, false,
							(NApplet) focusLosingNApplet);
					((NApplet) focusGainingNApplet).focusGained(gainFocus);
				}
				if (focusLosingNApplet != null) {
					FocusEvent loseFocus = new FocusEvent(
							(NApplet) focusLosingNApplet, FOCUS_LOST, false,
							(NApplet) focusGainingNApplet);
					((NApplet) focusLosingNApplet).focusLost(loseFocus);
				}
				focusNibble = nib;
			}
		}
	}

	public void keyEvent(KeyEvent event) {
		if (focusNibble != null) {
			focusNibble.passKeyEvent(event);
		}
	}

	public void createNApplet(String nappletClassName, int x, int y) {
		createEmbeddedNApplet(nappletClassName, x, y);
	}

	public void createEmbeddedNApplet(String nappletClassName, int x, int y) {
		NApplet nap = NApplet.createNApplet(parentPApplet, nappletClassName,
				this);
		if (nap != null) {
			nap.initEmbeddedNApplet(parentPApplet, x, y);
			addNibble(nap);
			nap.g.format = ARGB;
		}
	}

	public void createWindowedNApplet(String nappletClassName, int x, int y) {
		NApplet nap = NApplet.createNApplet(parentPApplet, nappletClassName,
				this);
		if (nap != null) {
			@SuppressWarnings("unused")
			NFrame nFrame = new NFrame(parentPApplet, nap, x, y);
			addNibble(nap);
		}
	}

	public void killNApplet(NApplet napplet) {
		killList.add(napplet);
	}

}
