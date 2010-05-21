import napplet.*;

PFont mainFont;

String sketchText = 
  "NApplets can be written in separate tabs in the Processing Design Environment for convenience.\n\n" +
  "Here's a gallery of some of the Processing example sketches as NApplets.";

void setup() {
  size(800, 500);
  mainFont = loadFont("../../common/data/ArialMT-18.vlw");
  textMode(SCREEN);
  textAlign(CENTER, TOP);
  
  NitManager nitManager = new NitManager(this);

  nitManager.createNApplet("Scrollbar", 0, 100);
  nitManager.createNApplet("Pattern", 200, 100);
  nitManager.createNApplet("Convolution", 400, 100);
  nitManager.createNApplet("Animator", 600, 100);
  nitManager.createNApplet("BouncyBubbles", 0, 300);
  nitManager.createNApplet("FireCube", 200, 300);
  nitManager.createNApplet("Tickle", 400, 300);
  nitManager.createNApplet("UnlimitedSprites", 600, 300);
}

void draw() {
  background(50);
  stroke(255);
  fill(255);

  textFont(mainFont);
  text(sketchText, width/2, 20);
  
}

