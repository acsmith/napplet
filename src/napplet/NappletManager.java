package napplet;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class NappletManager {

	List<Napplet> nappletList = new ArrayList<Napplet>();
	
	PApplet parentPApplet;
	
	public NappletManager(PApplet pap) {
		super();
		parentPApplet = pap;
		parentPApplet.registerPre(this);
		parentPApplet.registerDraw(this);
		parentPApplet.registerMouseEvent(this);
	}
	
	public void addNapplet(Napplet nap) {
		nap.parentPApplet = parentPApplet;
		nappletList.add(nap);
	}

	public Napplet containingNapplet(int x, int y) {
		Napplet containingNapplet = null;
		for (Napplet nap: nappletList) {
			int xRel = x - nap.nappletX;
			int yRel = y - nap.nappletY;
			if (xRel >= 0 && yRel >= 0 && xRel < nap.width && yRel < nap.height) {
				containingNapplet = nap;
			}
		}
		return containingNapplet;
	}
	
	public void pre() {
		
	}
	
	public void draw() {
		for (Napplet nap : nappletList)
			nap.handleDraw();
	}
	
	public void mouseEvent(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		
		Napplet nap = containingNapplet(x, y);
		if (nap!=null) {
			event.translatePoint(-(nap.nappletX), -(nap.nappletY));
			nap.passMouseEvent(event);
		}
		
	}
}

