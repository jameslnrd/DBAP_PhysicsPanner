import com.cycling74.max.*;
import java.util.ArrayList;


import miPhysics.*;

public class DPapSpat extends MaxObject implements Executable
{
	int simRate = 100;
	int frameRate = 60;

	int nb_speakers = 20;

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


	private ArrayList<Vect3D> speakers = new ArrayList<>();
	Vect3D pos = new Vect3D();

	private ArrayList<Integer> indexes = new ArrayList<>();
	private ArrayList<double[]> distances = new ArrayList<>();
	private ArrayList<double[]> ampli = new ArrayList<>();

	double scale = 0.02;

	//double[] distances;
	//double[] ampli;


	private MaxClock clock;
	private PhysicalModel mdl;

	private static final String[] INLET_ASSIST = new String[]{
		"inlet 1 help"
	};

	private static final String[] OUTLET_ASSIST = new String[]{
		"outlet 1 help"
	};
	
	public DPapSpat(Atom[] args)
	{
		declareInlets(new int[]{DataTypes.ALL});
		declareOutlets(new int[]{DataTypes.LIST, DataTypes.LIST, DataTypes.LIST, DataTypes.FLOAT});
		
		setInletAssist(INLET_ASSIST);
		setOutletAssist(OUTLET_ASSIST);

		mdl = new PhysicalModel(300, 100);

		mdl.addGround3D("h1",  h1);
		mdl.addGround3D("h2",  h2);
		mdl.addGround3D("h3",  h3);
		mdl.addGround3D("h4",  h4);
		mdl.addGround3D("h5",  h5);
		mdl.addGround3D("h6",  h6);
		mdl.addGround3D("h7",  h7);
		mdl.addGround3D("h8",  h8);

		mdl.addGround3D("h9",  h9);
		mdl.addGround3D("h10",  h10);
		mdl.addGround3D("h11",  h11);
		mdl.addGround3D("h12",  h12);
		mdl.addGround3D("h13",  h13);
		mdl.addGround3D("h14",  h14);
		mdl.addGround3D("h15",  h15);
		mdl.addGround3D("h16",  h16);

		mdl.addGround3D("h17",  h17);
		mdl.addGround3D("h18",  h18);
		mdl.addGround3D("h19",  h19);
		mdl.addGround3D("h20",  h20);


		mdl.setGravity(0.000);
		mdl.setFriction(0.001);


		mdl.init();

		post("The speaker setup has been initialised");


		clock = new MaxClock(this);
		clock.delay(0);

		sendSpeakerPos();
		sendMassPos();

	}


	private void addSource(){
		int number = indexes.size();
		String name = "m_"+indexes.size();

		mdl.addMass3D(name, 10, new Vect3D(0, 0, 0  ), new Vect3D(0,0,0));
		indexes.add(mdl.getNumberOfMats()-1);

		for(int i = 0; i < nb_speakers; i++){
			mdl.addContact3D("sp_m_"+i, 0.2, 0.0, 0., name, "h"+(i+1));
		}
		mdl.addPlaneContact("p", 0, 0.01, 0.01, 2, 0, name);

		distances.add(new double[nb_speakers]);
		ampli.add(new double[nb_speakers]);

		for(int i = 0; i < nb_speakers; i++){
			distances.get(number)[i] = 10;
			ampli.get(number)[i] = 0;
		}
		post("Added a new source : " + indexes.get(indexes.size()-1));


	}

	public void setSources(int nb){
		while(indexes.size()>0){
			int curLast = indexes.get(indexes.size()-1);
			mdl.removeMatAndConnectedLinks(curLast);
			post("Removing previous sources a new source : " + curLast);
			indexes.remove(indexes.size()-1);
		}
		for(int i = 0; i < nb; i++)
			addSource();
	}

	public void start(){
		post("Starting the physical computation...");
		clock.delay(0);
		sendSpeakerPos();
		sendMassPos();
	}

	public void stop(){
		post("Stopping the physical computation...");
		clock.unset();
	}




	public void execute(){
		bang();
		clock.delay(1000/frameRate);
	}
    
	public void bang()
	{
		mdl.computeNSteps(simRate / frameRate);


		for(int chans = 0; chans < indexes.size(); chans++){
			calcGains(chans);
		}
		sendSpeakerPos();
		sendMassPos();

		//float[] list = new float[nb_speakers];
		//Vect3D pos = new Vect3D(0,0,0);

		/*
		Atom[] l2 = new Atom[4];
		
		for (int i = 0; i < nbParticles; i++){

			pos = mdl.getMatPosAt(1+i);

			
			l2[0] = Atom.newAtom(i+1);
			l2[1] = Atom.newAtom((float)(pos.x/1000.+0.5));
			l2[2] = Atom.newAtom((float)(pos.y/1000.+0.5));
			l2[3] = Atom.newAtom((float)(pos.z/1000.));

			MaxSystem.sendMessageToBoundObject("receiver", "setnode", l2);


			list[3*i+0]= (float)pos.x;
			list[3*i+1]= (float)pos.y;
			list[3*i+2]= (float)pos.z;
		}

		l2[0] = Atom.newAtom((float)pos.x);
		l2[1] = Atom.newAtom((float)pos.x);
		l2[2] = Atom.newAtom((float)pos.x);
		*/
		//outlet(0,  ampli );
		//outlet(1,  distances );
		//outlet(2,  pOut );
		//outlet(3,  I );

	}


	private void calcGains(int nb){
		double sum = 0;
		Vect3D mPos = mdl.getMatPosAt(indexes.get(nb));
		Vect3D sPos;

		for(int i = 0; i < nb_speakers; i++){
			sPos = mdl.getMatPosAt(i);
			distances.get(nb)[i] = mPos.dist(sPos) + 0.1;
			sum +=  1/(Math.pow(distances.get(nb)[i], 2));

		}
		sum = Math.sqrt(sum);

		double a = Math.pow(10, -6./20.);
		double k = 2. * a / sum;

		double I = 0;
		for(int i = 0; i < nb_speakers; i++){
			double amp = k / (2*distances.get(nb)[i]*a);
			ampli.get(nb)[i] = amp;
			I += amp*amp;
		}

		//double[] pOut = new double[3];
		pos = mdl.getMatPosAt(indexes.get(nb));
		//pOut[0] = pos.x;
		//pOut[1] = pos.y;
		//pOut[2] = pos.z;
		Atom[] pOut = new Atom[3];
		pOut[0] = Atom.newAtom(pos.x);
		pOut[1] = Atom.newAtom(pos.y);
		pOut[2] = Atom.newAtom(pos.z);


		Atom[] gains = new Atom[nb_speakers];
		for(int i = 0; i < nb_speakers; i++) {
			gains[i] = Atom.newAtom(ampli.get(nb)[i]);
		}

		MaxSystem.sendMessageToBoundObject("spat"+(nb+1), "gains", gains);
		MaxSystem.sendMessageToBoundObject("spat"+(nb+1), "pos", pOut);
	}



	public void setPos(int nb, float x, float y, float z)
	{
		//post("channel number: " + nb);
		//post("nb active masses: " + indexes.size());
		if(nb <= indexes.size())
			mdl.setMatPosAt(indexes.get(nb-1), new Vect3D(x,y,z));
		else
			post("Trying to move a sound source outside of bounds!");
	}

	protected void notifyDeleted() {
		clock.unset();
	}


	private void sendSpeakerPos(){
		Atom[] l2 = new Atom[4];
		for (int i = 0; i < nb_speakers; i++){

			pos = mdl.getMatPosAt(i);

			l2[0] = Atom.newAtom(i+1);
			l2[1] = Atom.newAtom((float)(pos.x*scale + 0.5));
			l2[2] = Atom.newAtom((float)(pos.y*scale + 0.5));
			l2[3] = Atom.newAtom((float)(pos.z*scale * 0.01));

			MaxSystem.sendMessageToBoundObject("spkrs", "setnode", l2);

		}
	}

	private void sendMassPos(){
		Atom[] l2 = new Atom[4];
		for (int i = 0; i < indexes.size(); i++){

			pos = mdl.getMatPosAt(indexes.get(i));

			l2[0] = Atom.newAtom(i+1);
			l2[1] = Atom.newAtom((float)(pos.x*scale + 0.5));
			l2[2] = Atom.newAtom((float)(pos.y*scale + 0.5));
			l2[3] = Atom.newAtom((float)(pos.z*scale * 0.5));

			MaxSystem.sendMessageToBoundObject("masses", "setnode", l2);

		}
	}

	public void setRenderScale(double sc){
		this.scale = sc;
	}

    /*
	public void inlet(int i)
	{
		if (i ==1)
			clock.delay(0);
		else
			clock.unset();
	}
    
	public void inlet(float f)
	{
	}

	public void start(){
		post("Starting the physical computation...");
		clock.delay(0);
	}

	public void stop(){
		post("Stopping the physical computation...");
		clock.unset();
	}

	public void repulsionDist(float val){
		post("Changing the repulsion distance to: "+ val);
		mdl.changeDistParamOfSubset(val, "contacts");
	}

	public void frictionVal(float val){
		post("Changing the friction value to: "+ val);
		mdl.setFriction(val);
	}

	public void simSpeed(int val){
		post("Changing the sim speed to: " + val + "Hz");
		simRate = val;
	}
    
    
	public void list(Atom[] list)
	{
    	mdl.setMatPosAt(attrListStart, new Vect3D((list[0].getFloat()-0.5)*1000,(list[1].getFloat()-0.5)*1000, 0));
	}
	*/
    
}


