package napplet.test;

import napplet.NApplet;
import napplet.NAppletManager;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class NAppletChainingTest extends PApplet {
	
	NAppletManager nappletManager;
	
	int clickCount = 0;
	
	public void setup() {
		size(300, 300);
		
		nappletManager = new NAppletManager(this);
		
		nappletManager.createEmbeddedNApplet("BigNApplet", 50, 50);
	}
	
	public void draw() {
		background(255);
		fill(0);
		String s  = "" + frameCount + " frames,\n" + clickCount + " clicks.";
		textAlign(CENTER, CENTER);
		text(s, width/2, 11*height/12);
	}
	
	public void mouseClicked() {
		clickCount++;
	}

	public class BigNApplet extends NApplet {
				
		int clickCount = 0;

		public void setup() {
			size(200, 200);
			
			nappletManager.createEmbeddedNApplet("LittleNApplet", 50, 50);

		}
		
		public void draw() {
			background(0);
			fill(255);
			String s  = "" + frameCount + " frames,\n" + clickCount + " clicks.";
			textAlign(CENTER, CENTER);
			text(s, width/2, 7*height/8);
		}

		public void mouseClicked() {
			clickCount++;
		}
		
		public class LittleNApplet extends NApplet {
			
			int clickCount = 0;
			
			public void setup() {
				size(100, 100);
			}
			
			public void draw() {
				background(255);
				fill(0);
				String s  = "" + frameCount + " frames,\n" + clickCount + " clicks.";
				textAlign(CENTER, CENTER);
				text(s, width/2, height/2);
			}
			
			public void mouseClicked() {
				clickCount++;
			}
		}
	}
	
}
