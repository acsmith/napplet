import napplet.*;

NAppletManager nappletManager;
PFont mainFont;

String sketchText = 
  "A NApplet is an object that runs just like a Processing\n" +
  "sketch, but can run within the display space of another\n" +
  "sketch.  The two boxes below are NApplets running in\n" + 
  "this sketch.  Notice that each will respond to the mouse\n" +
  "when you move the mouse pointer into its NApplet, just\n" +
  "as a stand-alone sketch will only respond to the mouse\n" +
  "when the mouse is inside its window.";

void setup() {
  size(500, 500);
  mainFont = loadFont("../../common/data/ArialMT-18.vlw");
  textMode(SCREEN);
  
  nappletManager = new NAppletManager(this);
  nappletManager.createNApplet("MouseFollow", 25, 200);
  nappletManager.createNApplet("MouseFollow", 275, 200);
}

void draw() {
  background(50);
  stroke(255);
  fill(255);

  textFont(mainFont, 18);
  textAlign(CENTER, TOP);
  text(sketchText, width/2, height/20); 
}

public class MouseFollow extends NApplet {
  
  int x, y;
  
  void setup() {
    size(200, 200);
    x = width/2;
    y = height/2;
  }
  
  void draw() {
    if (focused) {
      x = (7*x + mouseX)/8;
      y = (7*y + mouseY)/8;
    }
    
    background(0);
    
    stroke(255);
    fill(150);

    ellipse(x, y, 50, 50);
  }    
}
