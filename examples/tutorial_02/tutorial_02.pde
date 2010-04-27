import napplet.*;

NAppletManager nappletManager;
PFont mainFont;

String sketchText = 
  "A NApplet is written just like a regular Processing\n" +
  "sketch, but within its own Class.  It has its own\n" +
  "setup() and draw() routines, its own variables, etc.\n\n"
  "The source code for the NApplet below is on the right.";

  int sketchTextWidth = textWidth(sketchText);
  
void setup() {
  size(600, 400);
  mainFont = loadFont("ArialMT-18.vlw");
  textMode(SCREEN);
  
  nappletManager = new NAppletManager(this);
  nappletManager.createNApplet("MouseFollow", 
    20+ sketchTextWidth/2 - 100, 200);
}

void draw() {
  background(50);
  stroke(255);
  fill(255);

  textFont(mainFont, 18);
  textAlign(LEFT, TOP);
  text(sketchText, 20, 20); 
}

public class MouseFollow extends NApplet {
  
  int x, y;
  
  void setup() {
    size(200, 200);
    mouseX = x = width/2;
    mouseY = y = height/2;
  }
  
  void draw() {
    x = (7*x + mouseX)/8;
    y = (7*y + mouseY)/8;
    
    background(0);
    stroke(255);
    fill(150);
    ellipse(x, y, 50, 50);
  }    
}
