import napplet.*;

NAppletManager nappletManager;
PFont mainFont, codeFont;

String titleText = "NApplets can be created in their own windows.";

String bottomText = "Create as many as you like.";

String button1Text = 
  "Click Here\nto Create a\nNice Window";

String button2Text = 
  "Click Here\nto Create a\nMean Window";

void setup() {
  size(400, 200);
  mainFont = loadFont("../../common/data/ArialMT-18.vlw");
  codeFont = loadFont("../../tutorial_01/data/CourierNewPS-BoldMT-14.vlw");
  textMode(SCREEN);
  textAlign(CENTER, CENTER);
  
  nappletManager = new NAppletManager(this);
}

void draw() {
  background(0);
  stroke(255);
  fill(100);
  
  rect(width/12, height/4, width/3, height/2);
  rect(7*width/12, height/4, width/3, height/2);
  
  textFont(mainFont);
  fill(255);
  text(titleText, width/2, height/8);
  text(bottomText, width/2, 7*height/8);
  text(button1Text, width/4, height/2);
  text(button2Text, 3*width/4, height/2);
}

void mousePressed() {
  if (mouseY >= height/4 && mouseY <= 3*height/4) {
    if (mouseX >= width/12 && mouseX <= 5*width/12) {
      nappletManager.createWindowedNApplet("NiceWindow",
      (int) random(100, 400), (int) random(100, 400));
    }
    else if (mouseX >= 7*width/12 && mouseX <= 11*width/12) {
      nappletManager.createWindowedNApplet("MeanWindow",
      (int) random(100, 400), (int) random(100, 400));
    }
  }
}

public class NiceWindow extends NApplet {
  
  String sketchText = 
  "This window can be\n" + 
  "closed with the controls\n" + 
  "on the title bar, because\n" +
  "nappletCloseable is true.\n" +
  "(Or the NApplet will close\n" + 
  "itself if you hit the X key.)";
  
  public void setup() {
    size(250, 250);
    nappletCloseable = true;
    textMode(SCREEN);
    textAlign(CENTER, CENTER);
  }
  
  public void draw() {
    background(0, 100, 0);
    stroke(255);
    fill(255);
    
    textFont(mainFont);
    text(sketchText, width/2, height/2);
  }
  
  public void keyPressed() {
    if (key=='x' || key=='X') exit();
  }
}


public class MeanWindow extends NApplet {
  
  String sketchText = 
  "This window can't be\n" + 
  "closed with the controls\n" + 
  "on the title bar, because\n" +
  "nappletCloseable is false.\n" +
  "(But the NApplet will close\n" + 
  "itself if you hit the X key.)";
  
  public void setup() {
    size(250, 250);
    nappletCloseable = false; // Not actually necessary, false by default.
    textMode(SCREEN);
    textAlign(CENTER, CENTER);
  }
  
  public void draw() {
    background(100, 0, 0);
    stroke(255);
    fill(255);
    
    textFont(mainFont);
    text(sketchText, width/2, height/2);
  }
  
  public void keyPressed() {
    if (key=='x' || key=='X') exit();
  }
}
