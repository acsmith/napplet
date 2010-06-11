package napplet;

import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class NitBase implements Nit, PConstants {

	public NAppletManager nappletManager;
	public PApplet parentPApplet;
	public PGraphics g;
	
	public int height, width;
	public int nitX, nitY;
	public boolean embeddedNit = true;

	public int mouseWheel;
	public int pmouseWheel;

	public int mouseX, mouseY;
	public int pmouseX, pmouseY;
	public boolean mousePressed;
	public int mouseButton;

	public char key;
	public int keyCode;
	public boolean keyPressed;

	public boolean focused = false;
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public NAppletManager getNAppletManager() {
		return nappletManager;
	}

	public void setNAppletManager(NAppletManager nappletManager) {
		this.nappletManager = nappletManager;
		this.parentPApplet = nappletManager.parentPApplet;
	}

	public int getPositionX() {
		return nitX;
	}

	public int getPositionY() {
		return nitY;
	}

	public void setPosition(int x, int y) {
		this.nitX = x;
		this.nitY = y;
	}

	public boolean isEmbedded() {
		return embeddedNit;
	}

	public boolean inputHit(int x, int y) {
		return true;
	}
	
	public abstract void runFrame();

	public void passEvent(InputEvent event) {
		if (event instanceof MouseWheelEvent) {
			pmouseWheel = mouseWheel;
			mouseWheel += ((MouseWheelEvent) event).getWheelRotation();
			mouseWheelMoved();
		} else if (event instanceof MouseEvent) {

			int id = ((MouseEvent) event).getID();
			if ((id == MouseEvent.MOUSE_DRAGGED)
					|| (id == MouseEvent.MOUSE_MOVED)) {
				pmouseX = mouseX;
				pmouseY = mouseY;
				mouseX = ((MouseEvent) event).getX();
				mouseY = ((MouseEvent) event).getY();
			}

			int modifiers = event.getModifiers();
			if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
				mouseButton = LEFT;
			} else if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
				mouseButton = CENTER;
			} else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
				mouseButton = RIGHT;
			}

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

			if (PApplet.platform == MACOSX) {
				if (((MouseEvent) event).isPopupTrigger()) {
					mouseButton = RIGHT;
				}
			}

		} else if (event instanceof KeyEvent) {
			this.key = ((KeyEvent) event).getKeyChar();
			this.keyCode = ((KeyEvent) event).getKeyCode();

			switch (((KeyEvent) event).getID()) {
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
		}
	}

	public void focusGained(FocusEvent gainFocus) {
		focused = true;
		focusGained();
	}

	public void focusLost(FocusEvent loseFocus) {
		focused = false;
		focusLost();
	}

	// Everything after this is empty and meant to be overridden by a subclass if
	// necessary.

	public void setup() {
	}

	public void draw() {
	}

	public void mouseWheelMoved() {
	}

	public void mousePressed() {
	}

	public void mouseReleased() {
	}

	public void mouseClicked() {
	}

	public void mouseDragged() {
	}

	public void mouseMoved() {
	}

	public void keyPressed() {
	}

	public void keyReleased() {
	}

	public void keyTyped() {
	}

	public void focusGained() {
	}

	public void focusLost() {
	}

}
