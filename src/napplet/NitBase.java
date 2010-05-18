package napplet;

import java.awt.event.FocusEvent;

import processing.core.PApplet;

public abstract class NitBase implements Nit {

	public void setup() {
	}

	public void draw() {
	}

	NAppletManager nappletManager;
	PApplet parentPApplet;
	int height, width;
	int nitX, nitY;
	boolean embeddedNit = true;
	
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

	}

	public PApplet getParentPApplet() {
		return parentPApplet;
	}

	public void setParentPApplet(PApplet parentPApplet) {
		this.parentPApplet = parentPApplet;
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

	public void runFrame() {
		// TODO Auto-generated method stub
	}
	
	public void passEvent(java.awt.Event event) {	
	}
	
	public void focusGained(FocusEvent gainFocus) {
	}

	public void focusLost(FocusEvent loseFocus) {
	}


}
