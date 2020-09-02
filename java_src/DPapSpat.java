import com.cycling74.max.*;
import java.util.ArrayList;


import miPhysics.Engine.*;

public class DPapSpat extends MaxObject implements Executable
{
	int simRate = 100;
	int frameRate = 60;

	int nb_speakers = 20;

	private ArrayList<Mass> sources = new ArrayList<>();
	private ArrayList<Mass> speakers = new ArrayList<>();
	private Driver3D driver = new Driver3D();

	Ground3D center = new Ground3D(0, new Vect3D(0,0,0));

	private ArrayList<double[]> distances = new ArrayList<>();
	private ArrayList<double[]> ampli = new ArrayList<>();

	double scale = 0.02;

	private MaxClock clock;
	private PhysicsContext phys;
	private PhyModel mdl;

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

		phys = new PhysicsContext(300, 100);
		mdl = phys.mdl();

		/*

		Vect3D spkPos[] = new Vect3D[nb_speakers];
		spkPos[0] = new Vect3D(5., -2., 1.2);
		spkPos[1] = new Vect3D(5., 2., 1.2);
		spkPos[2] = new Vect3D(2., 5, 1.2);
		spkPos[3] = new Vect3D(-2, 5, 1.2);
		spkPos[4] = new Vect3D(-5, 2., 1.2);
		spkPos[5] = new Vect3D(-5, -2., 1.2);
		spkPos[6] = new Vect3D(-2, -5, 1.2);
		spkPos[7] = new Vect3D(2, -5, 1.2);

		spkPos[8] = new Vect3D(4, -4., 4);
		spkPos[9] = new Vect3D(4, 0., 4);
		spkPos[10] = new Vect3D(4, 4, 4);
		spkPos[11] = new Vect3D(0, 4, 4);
		spkPos[12] = new Vect3D(-4, 4, 4);
		spkPos[13] = new Vect3D(-4, 0., 4);
		spkPos[14] = new Vect3D(-4, -4, 4);
		spkPos[15] = new Vect3D(0, -4, 4);

		spkPos[16] = new Vect3D(2, -1.5, 5);
		spkPos[17] = new Vect3D(2, 1.5, 5);
		spkPos[18] = new Vect3D(-2, 1.5, 5);
		spkPos[19] = new Vect3D(-2, -1.5, 5);


		// Create the fixed point elements corresponding to the speakers.
		for(int i = 0; i < nb_speakers; i++){
			Mass g = new Ground3D(0.1, spkPos[i]);
			mdl.addMass("h"+(i+1), g);
			speakers.add(g);
		}
		*/

		phys.setGlobalGravity(0.00, 0.0, 0.001);
		phys.setGlobalFriction(0);

		phys.init();

		post("The speaker setup has been initialised");


		clock = new MaxClock(this);
		clock.delay(0);

		sendSpeakerPos();
		sendMassPos();

	}



	public void addSpkr(int index, float x, float y, float z){
		//post("got some stuff " + index + " " + x + " " + y + " "+ z);

		// Create a new fixed point element for the speaker
		Vect3D loc = new Vect3D(x,y,z);
		Mass g = new Ground3D(0.1, loc);
		speakers.add(g);
		String name = "h"+(speakers.size());
		phys.mdl().addMass(name, g);
		nb_speakers = speakers.size();

		post("added speaker " + name + " at : " + loc.toString());

	}


	private void addSource(){
		int number = sources.size();
		String name = "m_"+sources.size();

		Mass3D m = new Mass3D(10, 0.1, new Vect3D(0,0,0), new Vect3D(4,0,0));
		sources.add(m);
		mdl.addMass(name, m);

		for(int i = 0; i < nb_speakers; i++){
			mdl.addInteraction("sp_m_"+sources.size()+"_"+i, new Contact3D(0.2, 0), m, speakers.get(i) );
		}

		mdl.addInteraction("p_"+sources.size(), new PlaneContact3D(0.1, 0.01, 2, 0), m);

		mdl.addInteraction("b_"+sources.size(), new Bubble3D(5, 0.01, 0.01), m, center);

		distances.add(new double[nb_speakers]);
		ampli.add(new double[nb_speakers]);

		for(int i = 0; i < nb_speakers; i++){
			distances.get(number)[i] = 10;
			ampli.get(number)[i] = 0;
		}
		post("Added a new source : " + m.getName());


	}

	public void setSources(int nb){

		sources.forEach((n) -> {
			mdl.removeMassAndConnectedInteractions(n);
			post("Removing previous source : " + n.getName());
		});
		sources.clear();

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


	public void removeSpeakers(){
		speakers.forEach((n) -> {
			phys.mdl().removeMassAndConnectedInteractions(n);
			post("Removing speaker : " + n.getName());
		});
		speakers.clear();
		nb_speakers = 0;
	}


	public void execute(){
		bang();
		clock.delay(1000/frameRate);
	}
    
	public void bang()
	{
		phys.computeScene();

		for(int chans = 0; chans < sources.size(); chans++){
			calcGains(chans);
		}
		//sendSpeakerPos();
		sendMassPos();

	}


	private void calcGains(int nb){

		double sum = 0;
		Vect3D mPos = sources.get(nb).getPos();
		Vect3D sPos;

		for(int i = 0; i < nb_speakers; i++){
			sPos = speakers.get(i).getPos();

			//sPos = mdl.getMatPosAt(i);
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

		Atom[] pOut = new Atom[3];
		pOut[0] = Atom.newAtom(mPos.x);
		pOut[1] = Atom.newAtom(mPos.y);
		pOut[2] = Atom.newAtom(mPos.z);

		Atom[] gains = new Atom[nb_speakers];
		for(int i = 0; i < nb_speakers; i++) {
			gains[i] = Atom.newAtom(ampli.get(nb)[i]);
		}

		MaxSystem.sendMessageToBoundObject("spat"+(nb+1), "gains", gains);
		MaxSystem.sendMessageToBoundObject("spat"+(nb+1), "pos", pOut);
	}



	public void setPos(int nb, float x, float y, float z)
	{
		if(nb <= sources.size()){
			driver.moveDriver(sources.get(nb-1));
			driver.applyPos(new Vect3D(x,y,z));
		}
		else
			post("Trying to move a sound source outside of bounds!");
	}

	protected void notifyDeleted() {
		clock.unset();
	}


	public void getSpeakerPos(){
		for (int i = 0; i < speakers.size(); i++)
			post("Speaker " + (i+1) + ": " + speakers.get(i).getPos().toString());

		sendSpeakerPos();
	}

	private void sendSpeakerPos(){
		Atom[] l2 = new Atom[4];
		for (int i = 0; i < speakers.size(); i++){

			Vect3D pos = speakers.get(i).getPos();
			l2[0] = Atom.newAtom(i+1);
			l2[1] = Atom.newAtom((float)(pos.x*scale + 0.5));
			l2[2] = Atom.newAtom((float)(pos.y*scale + 0.5));
			l2[3] = Atom.newAtom((float)(pos.z*scale * 0.5));

			MaxSystem.sendMessageToBoundObject("spkrs", "setnode", l2);

		}
	}

	private void sendMassPos(){
		Atom[] l2 = new Atom[4];
		for (int i = 0; i < sources.size(); i++){

			Vect3D pos = sources.get(i).getPos();
			l2[0] = Atom.newAtom(i+1);
			l2[1] = Atom.newAtom((float)(pos.x*scale + 0.5));
			l2[2] = Atom.newAtom((float)(pos.y*scale + 0.5));
			l2[3] = Atom.newAtom((float)(pos.z*scale * 0.5));

			MaxSystem.sendMessageToBoundObject("masses", "setnode", l2);
		}
	}

	public void setRenderScale(double sc){
		this.scale = sc;
		sendSpeakerPos();
		sendMassPos();
	}
    
}


