package napplet;

import java.awt.Frame;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class NFrame extends Frame {

	public NApplet napplet;

	public NFrame(PApplet pap, NApplet nap, int x, int y) {
		super();
		napplet = nap;
		napplet.initWindowedNApplet(pap, x, y, pap.sketchPath);
		add(napplet);
		napplet.frame = this;
		napplet.setupNAppletMessages();
		addComponentListener(napplet);
		setVisible(true);
		setResizable(false);
		java.awt.Insets insets = getInsets();
		setBounds(x, y, napplet.width + (insets.left + insets.right),
				napplet.height + (insets.top + insets.bottom));
		setTitle(nap.getClass().getName());
		toFront();
	}

}
