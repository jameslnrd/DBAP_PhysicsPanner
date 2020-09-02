

import miPhysics.*;
import peasy.*;

PeasyCam cam;
PhysicalModel mdl;
ModelRenderer renderer;

float scale = 10;

boolean applyFrc = false;


import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;

int mat_idx = 0;

int displayRate = 60;




  Vect3D h1 = new Vect3D(50, -20., 12);
  Vect3D h2 = new Vect3D(50, 20., 12);
  Vect3D h3 = new Vect3D(20, 50, 12);
  Vect3D h4 = new Vect3D(-20, 50, 12);
  Vect3D h5 = new Vect3D(-50, 20., 12);
  Vect3D h6 = new Vect3D(-50, -20., 12);
  Vect3D h7 = new Vect3D(-20, -50, 12);
  Vect3D h8 = new Vect3D(20, -50, 12);
  
  Vect3D h9 = new Vect3D(40, -40., 40);
  Vect3D h10 = new Vect3D(40, 00., 40);
  Vect3D h11 = new Vect3D(40, 40, 40);
  Vect3D h12 = new Vect3D(00, 40, 40);
  Vect3D h13 = new Vect3D(-40, 40, 40);
  Vect3D h14 = new Vect3D(-40, 0., 40);
  Vect3D h15 = new Vect3D(-40, -40, 40);
  Vect3D h16 = new Vect3D(0, -40, 40);
  
  Vect3D h17 = new Vect3D(20, -15, 50);
  Vect3D h18 = new Vect3D(20, 15, 50);
  Vect3D h19 = new Vect3D(-20, 15, 50);
  Vect3D h20 = new Vect3D(-20, -15, 50);


void setup() {
  frameRate(displayRate);
  
  cam = new PeasyCam(this, 100);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(500);
  cam.rotateX(radians(-90));

  //size(900, 750, P3D);
  fullScreen(P3D);
  background(0);

  // instantiate our physical model context
  mdl = new PhysicalModel(350, displayRate);
  renderer = new ModelRenderer(this);
  
  oscP5 = new OscP5(this,12000);
  myRemoteLocation = new NetAddress("127.0.0.1",12001);


  
  
  mdl.addGround3D("h1", 2,  h1);
  mdl.addGround3D("h2", 2,  h2);
  
  mdl.addGround3D("h3", 2,  h3);
  mdl.addGround3D("h4", 2,  h4);
  
  mdl.addGround3D("h5", 2,  h5);
  mdl.addGround3D("h6", 2,  h6);
  
  mdl.addGround3D("h7", 2,  h7);
  mdl.addGround3D("h8", 2,  h8);


  mdl.addGround3D("h9", 2,  h9);
  mdl.addGround3D("h10", 2,  h10);
  mdl.addGround3D("h11", 2,  h11);
  
  mdl.addGround3D("h12", 2,  h12);
  
  mdl.addGround3D("h13", 2,  h13);
  mdl.addGround3D("h14", 2,  h14);
  mdl.addGround3D("h15", 2,  h15);
  
  mdl.addGround3D("h16", 2,  h16);


  mdl.addGround3D("h17", 2,  h17);
  mdl.addGround3D("h18", 2,  h18);
  mdl.addGround3D("h19", 2,  h19);
  mdl.addGround3D("h20", 2,  h20);
   
 
  mdl.addMass3D("m", 10, 2,  new Vect3D(-5, -5, 5  ).mult(scale), new Vect3D(0,0,0));
  //mdl.addPlaneContact("p", 0, 0.01, 0.0, 2, 0, "m");

  mat_idx = mdl.getNumberOfMats()-1;
  
  //mdl.addSpringDamper3D("s", 3*scale, 0.001, 0.01, "m", "h19");

  for(int i = 0; i < 20; i++){
    mdl.addContact3D("sp_m_"+i, 0.0, 0., "m", "h"+(i+1));
  }


  //mdl.setGravity(0.0001);
  mdl.setFriction(0.00);
    
  renderer.displayMats(true);
  renderer.setScaling(matModuleType.Mass3D, 1);
  renderer.setColor(linkModuleType.Rope3D, 155, 200, 200, 255);
  renderer.setSize(linkModuleType.Rope3D, 3);

  // initialise the model before starting calculations.
  mdl.init();
  
  cam.setDistance(40);  // distance from looked-at point
} 

void draw() {

  mdl.draw_physics();

  FloatList distances  = new FloatList();
  FloatList ampli  = new FloatList();


  for(int i = 0; i < 20; i++){
      distances.append((float)(mdl.getLinkDistanceAt(i)+ 0.1));
       //println("Distance : " + distances.get(distances.size()-1));
  }
  
  float sum = 0;
  for(float d: distances){
    sum +=  1/(d*d);
  }
  sum = sqrt(sum);
  
  float a = pow(10, -6./20.);
  float k = 2. * a / sum;
  
  OscMessage myMessage = new OscMessage("/gains");
  for(int i = 0; i < 20; i++){
    float amp = k / (2*distances.get(i)*a);
    ampli.append(amp);
    //println("Amplitude : " + amp);    
    myMessage.add(amp);
  }
  oscP5.send(myMessage, myRemoteLocation); 

  
  float I = 0;
  for(float am: ampli){
    I += am*am;
  }
  println("Global Intensity :" + I);

  directionalLight(251, 102, 126, 0, -1, 0);
  ambientLight(102, 102, 102);

  background(0);
  
  //drawSpeakerNetwork();

  strokeWeight(1);
  //drawGrid(40, 4);
  drawPlane(2, 0, 160); 
  
  renderer.renderModel(mdl);
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
  
  if(theOscMessage.checkAddrPattern("/pos")==true) {
    /* check if the typetag is the right one. */
    if(theOscMessage.checkTypetag("fff")) {
      /* parse theOscMessage and extract the values from the osc message arguments. */
      float first = theOscMessage.get(0).floatValue();
      float sec = theOscMessage.get(1).floatValue();  
      float third = theOscMessage.get(2).floatValue();  
      
      mdl.setMatPosAt(mat_idx,new Vect3D(first, sec, third));
      
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
