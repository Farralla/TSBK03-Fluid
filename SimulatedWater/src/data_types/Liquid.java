package data_types;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import marching_cubes.MCCube;
import marching_cubes.MCGrid;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Utils.Debug;
import Utils.Timer;

/**
 * 
 * @author Martin
 *
 */
public class Liquid {
	public static final boolean multiThread = true;
	private boolean mIsStarted = false;
	
	public static final int DRAW_PARTICLES = 0;
	public static final int DRAW_SURFACE = 1;
	public static final int DRAW_TRIANGLES = 2;
	public static final int DRAW_GRID = 3;
	
	//Timeout
	public static final int TIME_OUT = 100; //ms timeout time

	// drawMode
	private int mDrawMode = DRAW_SURFACE;

	// Member variables
	private int mNumberOfParticles;
	private float mParticleMass;
	private Vector<Particle> mParticleList;
	private Vector<CollidableSphere> mCollidables;
	private Vector4f mColor;
	private MCGrid mMCGrid;
	
	public static Vector3f g = new Vector3f(0,-20,0); // Gravity
	public static double dT = 0.01;

	// Liquid bounds
	private Boundaries mBoundaries;

	private Calculator mCalc1, mCalc2, mCalc3, mCalc4;

	/**
	 * Constructor Creates a liquid with a specified number of particles
	 * 
	 * @param numberOfParticles
	 *            The number of particles in the liquid
	 * @param particleMass
	 *            The mass of each particle
	 */
	public Liquid(int numberOfParticles, float size) {
		setNumberOfParticles(numberOfParticles);
		mParticleList = new Vector<Particle>();
		mBoundaries = new Boundaries(size);
		mMCGrid = new MCGrid(size, MCGrid.DEFAULT_GRID_SCALE, MCGrid.DEFAULT_ISO_LEVEL);
		mCollidables = new Vector<CollidableSphere>();
		init();
	}

	/**
	 * Constructor Creates a liquid with a specified number of particles Creates
	 * MCgrid with input parameters
	 * 
	 * @param numberOfParticles
	 * @param size
	 * @param gridScale
	 * @param isoLevel
	 */
	public Liquid(int numberOfParticles, float size, float gridScale, float isoLevel) {
		setNumberOfParticles(numberOfParticles);
		mParticleList = new Vector<Particle>();
		float boundSize = (float) (Math.sqrt(0.5)*size);
		mBoundaries = new Boundaries(boundSize);
		mMCGrid = new MCGrid(size, gridScale, isoLevel);
		mCollidables = new Vector<CollidableSphere>();

		init();
	}

	public void init() {
		Random random = new Random();
		Particle.setValues();
		
		for (int i = 0; i < mNumberOfParticles; i++) {
			Vector3f startingPos = new Vector3f(
					mBoundaries.xHigh/4 * random.nextFloat(),
					mBoundaries.yHigh * random.nextFloat(),
					mBoundaries.xHigh * random.nextFloat());
			mParticleList.add(new Particle(this, startingPos));
		}
		
		mCollidables.add(new CollidableSphere(this,new Vector3f(0.15f,0.05f,0.15f), 0.05f));
		
		int partSize = (int) Math.ceil(mParticleList.size() / 4);
		mCalc1 = new Calculator(this, 1,mParticleList.subList(0, partSize));
		mCalc2 = new Calculator(this,2, mParticleList.subList(partSize, 2*partSize));
		mCalc3 = new Calculator(this, 3, mParticleList.subList(2*partSize, 3*partSize));
		mCalc4 = new Calculator(this,4, mParticleList.subList(3 * partSize,4*partSize));

	}
	
	private void runCalculationThreads(int mode){	
		Thread t2 = new Thread(mCalc1);
		t2.setPriority(Thread.MAX_PRIORITY);
		
		Thread t3 = new Thread(mCalc2);
		t3.setPriority(Thread.MAX_PRIORITY);
		
		Thread t4 = new Thread(mCalc3);
		t4.setPriority(Thread.MAX_PRIORITY);
		
		Thread t5 = new Thread(mCalc4);
		t5.setPriority(Thread.MAX_PRIORITY);
		
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		
		try {
			t2.join();
			t3.join();
			t4.join();
			t5.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	public void update() {
		
		
		
		Timer timer = new Timer();
		timer.off();
		timer.init();
		mMCGrid.resetScalarField();
		timer.update();
		timer.println("Reset scalar field");

		if (!multiThread) {
			for (Particle particle : mParticleList) {
				// Calculate densities and pressures of particles
				particle.updateDensityAndPressure(this);
			}
			for (Particle particle : mParticleList) {
				// Update particle forces
				particle.updateForces(this);
			}
			// Update particle velocities, positions
			for (Particle particle : mParticleList) {
				particle.update(this);
			}
		}
		else{
			runCalculationThreads(Calculator.CALC_DENSITIES_PRESSURES);
			runCalculationThreads(Calculator.CALC_FORCES);
			runCalculationThreads(Calculator.CALC_UPDATE);
		}
		
		timer.update();
		timer.println("updated particles");
		
		//Update collidable objects
//		for(CollidableSphere c : mCollidables){
//			c.update();
//		}
		mCollidables.get(0).update();
		

		timer.update();
		timer.println("Collision objects update");

		// Polygonise surface
		mMCGrid.march();

		timer.update();
		timer.println("Marching cubes ");
		
		//Waits after one iteration
		waitOnStart();
	}

	private void waitOnStart() {
		while(!mIsStarted){
			synchronized(this){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	public Particle getParticle(int particleID) {
		Particle particle = mParticleList.get(particleID);
		return particle;

	}

	public Vector<Particle> getParticleList() {
		return mParticleList;
	}

	public Vector4f getColor() {
		return mColor;
	}

	public void setColor(Vector4f color) {
		mColor = color;
	}

	public int getNumberOfParticles() {
		return mNumberOfParticles;
	}

	public float getParticleMass() {
		return mParticleMass;
	}

	public void setNumberOfParticles(int numberOfParticles) {
		mNumberOfParticles = numberOfParticles;
	}

	public void setParticleMass(float particleMass) {
		mParticleMass = particleMass;
	}

	public Boundaries getBoundaries() {
		return mBoundaries;
	}

	public MCGrid getGrid() {
		return mMCGrid;
	}

	public int drawMode() {
		return mDrawMode;
	}

	/**
	 * Set the drawmode Allowed values {@link Liquid.DRAW_PARTICLES,
	 * Liquid.DRAW_SURFACE, Liquid.DRAW_GRID}
	 * 
	 * @param drawMode
	 */
	public void setDrawMode(int drawMode) {
		mDrawMode = drawMode;
	}
	
	/**
	 * Gravity force in negative y-axis
	 * 
	 * @return
	 */
	public static Vector3f gravity() {
		return new Vector3f(g);
	}

	public class Boundaries {
		public String type;
		private Vector3f mPosition;
		private float mSize;
		// Liquid bounds
		public float xLow;
		public float xHigh;
		public float yLow;
		public float yHigh;
		public float zLow;
		public float zHigh;

		private boolean mSideConstraintsOn = true;

		public Boundaries(float xL, float xH, float yL, float yH, float zL, float zH) {
			type = "box";
			xLow = xL;
			xHigh = xH;
			yLow = yL;
			yHigh = yH;
			zLow = zL;
			zHigh = zH;
		}

		public Boundaries(float size) {
			type = "box";
			xLow = 0;
			xHigh = size;
			yLow = 0;
			yHigh = size;
			zLow = 0;
			zHigh = size;
			mPosition = new Vector3f(size/2,size/2,size/2);
			mSize = size;
		}
		
		public Vector3f getPosition(){
			return mPosition;
		}
		
		public float getSize(){
			return mSize;
		}

		public boolean isSideConstraintsOn() {
			return mSideConstraintsOn;
		}

		public void setSideConstraintsOn(boolean b) {
			mSideConstraintsOn = b;
		}
	}

	public class Calculator implements Runnable {
		public static final int CALC_DENSITIES_PRESSURES = 0;
		public static final int CALC_FORCES = 1;
		public static final int CALC_UPDATE = 2;

		private List<Particle> mParticles;
		private Liquid mLiquid;
		private Boolean run;
		private int mMode;
		private int mId;

		public Calculator(Liquid liquid, int id, List<Particle> particleList) {
			 mParticles = particleList;
			Debug.println("List parts sizes " +  mParticles.size(), Debug.MAX_DEBUG);
			mLiquid = liquid;
			mId = id;
			run = false;
			mMode = CALC_DENSITIES_PRESSURES;
		}

		@Override
		public void run() {

				switch (mMode) {
				case CALC_DENSITIES_PRESSURES:
					for (Particle particle :  mParticles) {
						particle.updateDensityAndPressure(mLiquid);
					}
					break;
				case CALC_FORCES:
					for (Particle particle :  mParticles) {
						particle.updateForces(mLiquid);
					}
					break;
				case CALC_UPDATE:
					for (Particle particle :  mParticles) {
						particle.update(mLiquid);
					}
					break;
				}
				

				switchMode();
		}

		private void switchMode() {
			mMode++;
			if(mMode >= 3)
				mMode = 0;
		}

		public synchronized void setRun(boolean b) {
			run = b;
			this.notifyAll();
		}

		public synchronized Boolean getRun() {
			return run;
		}
		
	}

	public void setIsStarted(boolean b) {
		mIsStarted = b;
		
	}

	public boolean isStarted() {
		return mIsStarted;
	}

	public Vector<CollidableSphere> getCollidables() {
		return mCollidables;
	}

	public static void setGravity(Vector3f vec) {
		g = vec;
	}
}
