import napplet.*;

PFont mainFont;

String sketchText = 
  "NApplets can be written in separate tabs in the Processing Design Environment for convenience.\n\n" +
  "Here's a gallery of some of the Processing example sketches as NApplets.";

void setup() {
  size(800, 500);
  mainFont = loadFont("ArialMT-18.vlw");
  textMode(SCREEN);
  textAlign(CENTER, TOP);
  
  NAppletManager nappletManager = new NAppletManager(this);

  nappletManager.createNApplet("Scrollbar", 0, 100);
  nappletManager.createNApplet("Pattern", 200, 100);
  nappletManager.createNApplet("Convolution", 400, 100);
  nappletManager.createNApplet("Animator", 600, 100);
  nappletManager.createNApplet("BouncyBubbles", 0, 300);
  nappletManager.createNApplet("FireCube", 200, 300);
  nappletManager.createNApplet("Tickle", 400, 300);
  nappletManager.createNApplet("UnlimitedSprites", 600, 300);
}

void draw() {
  background(50);
  stroke(255);
  fill(255);

  textFont(mainFont);
  text(sketchText, width/2, 20);
  
}

