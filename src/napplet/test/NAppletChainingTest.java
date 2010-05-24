package napplet.test;

import napplet.NApplet;
import napplet.NAppletManager;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class NAppletChainingTest extends PApplet {

	NAppletManager nappletManager;

	int clickCount = 0;
	int keyCount = 0;

	public void setup() {
		size(500, 500);

		nappletManager = new NAppletManager(this);

		nappletManager.createEmbeddedNApplet("BigNApplet", 50, 50);
	}

	public void draw() {
		background(255);
		stroke(0);
		strokeWeight(10);
		noFill();
		if (focused) rect(10, 10, width - 20, height - 20);
		fill(0);
		textAlign(CENTER, CENTER);
		text(statString(mouseX, mouseY, 0, clickCount, keyCount), width / 2,
				19 * height / 20);
	}

	public String statString(int mx, int my, int wheel, int mouseClicks,
			int keyPresses) {
		return "Mouse: " + mx + ", " + my + "\nclicks: " + mouseClicks
				+ ", wheel: " + wheel + ", keypresses: " + keyPresses;
	}

	public void mouseClicked() {
		clickCount++;
	}

	public void keyPressed() {
		keyCount++;
	}
	
	public class BigNApplet extends NApplet {

		int clickCount = 0;
		int keyCount = 0;

		public void setup() {
			size(400, 400);

			nappletManager.createEmbeddedNApplet("LittleNApplet", 50, 50);

		}

		public void draw() {
			background(0);
			stroke(255);
			strokeWeight(10);
			noFill();
			if (focused)
				rect(10, 10, width - 20, height - 20);
			fill(255);
			textAlign(CENTER, CENTER);
			text(statString(mouseX, mouseY, mouseWheel, clickCount, keyCount),
					width / 2, 15 * height / 16);
		}

		public void mouseClicked() {
			clickCount++;
		}

		public void keyPressed() {
			keyCount++;
		}

		public class LittleNApplet extends NApplet {

			int clickCount = 0;
			int keyCount = 0;

			public void setup() {
				size(300, 300);
			}

			public void draw() {
				background(255);
				stroke(0);
				strokeWeight(10);
				noFill();
				if (focused)
					rect(10, 10, width - 20, height - 20);
				fill(0);
				textAlign(CENTER, CENTER);
				text(statString(mouseX, mouseY, mouseWheel, clickCount,
						keyCount), width / 2, height / 2);
			}

			public void mouseClicked() {
				clickCount++;
			}

			public void keyPressed() {
				keyCount++;
			}
		}
	}

}
