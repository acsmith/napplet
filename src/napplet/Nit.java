package napplet;

import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;

import processing.core.PApplet;

public interface Nit {

	public abstract int getPositionX();

	public abstract int getPositionY();

	public abstract void setPosition(int x, int y);

	public abstract int getWidth();

	public abstract int getHeight();

	public abstract boolean isEmbedded();

	public abstract PApplet getParentPApplet();
	
	public abstract void setParentPApplet(PApplet parentPApplet);
	
	public abstract NAppletManager getNAppletManager();
	
	public abstract void setNAppletManager(NAppletManager nappletManager);
	
	public abstract void setup();
	
	public abstract void draw();
	
	public abstract void passEvent(InputEvent event);
	
	public abstract void focusGained(FocusEvent gainFocus);

	public abstract void focusLost(FocusEvent loseFocus);
	
	public abstract void runFrame();

}