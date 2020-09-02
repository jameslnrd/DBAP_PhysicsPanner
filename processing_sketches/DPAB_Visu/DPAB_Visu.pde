
import peasy.*;

PeasyCam cam;

float scale = 10;

boolean applyFrc = false;


import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;

int mat_idx = 0;

int displayRate = 60;

ArrayList<PVector> sources = new ArrayList<PVector>();
ArrayList<PVector> speakers = new ArrayList<PVector>();


void setup() {
  frameRate(displayRate);
  
  cam = new PeasyCam(this, 100);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(500);
  cam.rotateX(radians(-90));

  size(900, 750, P3D);
  //fullScreen(P3D);
  background(0);

  
  oscP5 = new OscP5(this,777);
  myRemoteLocation = new NetAddress("127.0.0.1",12001);
  
  for(int i = 0; i < 64; i++){
    sources.add(new PVector(0,0,0));
    speakers.add(new PVector(0,0,0));
  }

  
  cam.setDistance(40);  // distance from looked-at point
} 


void drawSource(PVector pos){
  pushMatrix();
  fill(255, 0, 0);
  noStroke();
  translate(pos.x, pos.y, pos.z);
  sphere(1);
  popMatrix();
}

void drawSpeaker(PVector pos){
  fill(0, 125, 0);
  pushMatrix();
  noStroke();
  translate(pos.x, pos.y, pos.z);
  sphere(1);
  popMatrix();
}

void draw() {

  directionalLight(251, 102, 126, 0, -1, 0);
  ambientLight(102, 102, 102);

  background(0);
  
  //drawSpeakerNetwork();

  strokeWeight(1);
  //drawGrid(40, 4);
  drawPlane(2, 0, 160); 

    
  for(PVector spkr: speakers){
    drawSpeaker(spkr);
  }
  
  for(PVector src: sources){
    drawSource(src);
  } 
  
  
}

void keyPressed(){
  if(keyCode == ' ')
    applyFrc = true;
}

void keyReleased(){
  if(keyCode == ' ')
    applyFrc = false;
}



void oscEvent(OscMessage theOscMessage) {
  /* check if theOscMessage has the address pattern we are looking for. */
  
  if(theOscMessage.checkAddrPattern("/source")==true) {
    /* check if the typetag is the right one. */
    if(theOscMessage.checkTypetag("ifff")) {
      /* parse theOscMessage and extract the values from the osc message arguments. */
      int idx = theOscMessage.get(0).intValue();
      float x = theOscMessage.get(1).floatValue();  
      float y = theOscMessage.get(2).floatValue();  
      float z = theOscMessage.get(3).floatValue();  

      sources.set(idx,new PVector(10*x,10*y,10*z));
      
      //print("### received an osc message /test with typetag ifs.");
      //println(" values: "+firstValue+", "+secondValue+", "+thirdValue);
      return;
    }  
  }
  else if(theOscMessage.checkAddrPattern("/speaker")==true) {
    /* check if the typetag is the right one. */
    if(theOscMessage.checkTypetag("ifff")) {
      /* parse theOscMessage and extract the values from the osc message arguments. */
      int idx = theOscMessage.get(0).intValue();
      float x = theOscMessage.get(1).floatValue();  
      float y = theOscMessage.get(2).floatValue();  
      float z = theOscMessage.get(3).floatValue();  

      speakers.set(idx,new PVector(10*x,10*y,10*z));
      
      //print("### received an osc message /test with typetag ifs.");
      //println(" values: "+firstValue+", "+secondValue+", "+thirdValue);
      return;
    }  
  } 
  println("### received an osc message. with address pattern "+theOscMessage.addrPattern());
}



/*
void drawSpeakerNetwork(){
  stroke(255);
  noFill();
  
  beginShape();
  vertex((float)h1.x, (float)h1.y, (float)h1.z);
  vertex((float)h2.x, (float)h2.y, (float)h2.z);
  vertex((float)h3.x, (float)h3.y, (float)h4.z);
  vertex((float)h5.x, (float)h5.y, (float)h5.z);
  vertex((float)h6.x, (float)h6.y, (float)h6.z);
  vertex((float)h7.x, (float)h7.y, (float)h7.z);
  vertex((float)h8.x, (float)h8.y, (float)h8.z);
  vertex((float)h1.x, (float)h1.y, (float)h1.z);
  endShape(CLOSE);
}*/



//****************************************************************************//

void drawPlane(int orientation, float position, float size){
  stroke(255);
  noFill();
  
  beginShape();
  if(orientation ==2){
    vertex(-size, -size, position);
    vertex( size, -size, position);
    vertex( size, size, position);
    vertex(-size, size, position);
  } else if (orientation == 1) {
    vertex(-size,position, -size);
    vertex( size,position, -size);
    vertex( size,position, size);
    vertex(-size,position, size);  
  } else if (orientation ==0) {
    vertex(position, -size, -size);
    vertex(position, size, -size);
    vertex(position, size, size);
    vertex(position,-size, size);
  }
  endShape(CLOSE);
}
