package napplet.test;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MouseWheelEavesdropper implements MouseWheelListener {

	public MouseWheelEavesdropper(Component c) {
		c.addMouseWheelListener(this);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		System.out.println("I see the mouse wheel moving!");
	}

}
