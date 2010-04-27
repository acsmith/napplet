import napplet.*;

NAppletManager nappletManager;
PFont mainFont, codeFont;

String sketchText = 
  "A NApplet is written just like a regular\n" +
  "Processing sketch, but within its own\n" + 
  "class.  It has its own setup() and\n" + 
  "draw() routines, its own variables, etc.\n" +
  "The source code for the NApplet\n" + 
  "below is on the right, or you can see it\n" +
  "in the code for this sketch.";

int sketchTextWidth;

String codeText = 
  "public class MouseFollow\n" + 
  "  extends NApplet {\n" +
  "  \n" +
  "  int x, y;\n" +
  "  \n" +
  "  public void setup() {\n" +
  "    size(200, 200);\n" +
  "    mouseX = x = width/2;\n" +
  "    mouseY = y = height/2;\n" +
  "  }\n" +
  "  \n" +
  "  public void draw() {\n" +
  "    x = (7*x + mouseX)/8;\n" +
  "    y = (7*y + mouseY)/8;\n" +
  "    \n" +
  "    background(0);\n" +
  "    stroke(255);\n" +
  "    fill(150);\n" +
  "    ellipse(x, y, 50, 50);\n" +
  "  }\n" +
  "}";

void setup() {
  size(600, 400);
  mainFont = loadFont("ArialMT-18.vlw");
  codeFont = loadFont("CourierNewPS-BoldMT-14.vlw");
  textMode(SCREEN);
  textAlign(LEFT, TOP);
  
  sketchTextWidth = (int) textWidth(sketchText);
  
  nappletManager = new NAppletManager(this);
  nappletManager.createNApplet("MouseFollow", 
    sketchTextWidth/2 - 80, 180);
}

void draw() {
  background(50);
  stroke(255);
  fill(255);

  textFont(mainFont);
  text(sketchText, 20, 20);
  
  textFont(codeFont);
  text(codeText, 340, 20);
}

public class MouseFollow extends NApplet {
  
  int x, y;
  
  public void setup() {
    size(200, 200);
    mouseX = x = width/2;
    mouseY = y = height/2;
  }
  
  public void draw() {
    x = (7*x + mouseX)/8;
    y = (7*y + mouseY)/8;
    
    background(0);
    stroke(255);
    fill(150);
    ellipse(x, y, 50, 50);
  }    
}
