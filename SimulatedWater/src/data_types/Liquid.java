package data_types;

import java.util.Random;
import java.util.Vector;

import marching_cubes.MCCube;
import marching_cubes.MCGrid;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Utils.Debug;
import Utils.Timer;
import de.lessvoid.nifty.controls.slider.builder.SliderBuilder;

/**
 * 
 * @author Martin
 *
 */
public class Liquid {
	public static final int DRAW_PARTICLES = 0;
	public static final int DRAW_SURFACE = 1;
	public static final int DRAW_GRID = 2;
	
	//drawMode
	private int mDrawMode = DRAW_SURFACE;
	
	//Member variables
	private int mNumberOfParticles;
	private float mParticleMass;
	private float mParticleSize=1;
	private Vector<Particle> mParticleList;
	private Vector4f mColor;
	private MCGrid mMCGrid;
	
	//Liquid bounds
	private Boundaries mBoundaries;
	
	/**
	 * Constructor Creates a liquid with a specified number of particles
	 * TODO Additional information about the liquid in this constructor
	 * @param numberOfParticles The number of particles in the liquid
	 * @param particleMass The mass of each particle
	 */
	public Liquid(int numberOfParticles, float size){
		setNumberOfParticles(numberOfParticles);
		mParticleList = new Vector<Particle>();
		mBoundaries = new Boundaries(size);
		mMCGrid = new MCGrid(size,MCGrid.DEFAULT_GRID_SCALE,MCGrid.DEFAULT_ISO_LEVEL);
	}
	
	/**
	 * Constructor Creates a liquid with a specified number of particles
	 * Creates MCgrid with input parameters
	 * @param numberOfParticles
	 * @param size
	 * @param gridScale
	 * @param isoLevel
	 */
	public Liquid(int numberOfParticles, float size, float gridScale, float isoLevel){
		setNumberOfParticles(numberOfParticles);
		mParticleList = new Vector<Particle>();
		mBoundaries = new Boundaries(size);
		mMCGrid = new MCGrid(size,gridScale,isoLevel);
		
		init();
	}
	
	public void init(){
		Random random = new Random();
		for(int i = 0; i < mNumberOfParticles;i++){
			Vector3f startingPos = new Vector3f(
					mBoundaries.xHigh*random.nextFloat(),
					mBoundaries.yHigh*random.nextFloat(),
					mBoundaries.xHigh*random.nextFloat());
			mParticleList.add(new Particle(this,startingPos));
			
		}
	}
	
	public void update(){
		mMCGrid.resetScalarField();
		Timer timer = new Timer();
		timer.off();
		//Calculate densities and pressures of particles
		for(Particle particle:mParticleList){
			particle.updateDensityAndPressure(this);
		}
		timer.update();
		timer.println("update Desnsities and pressures");
		//Update particle forces, velocities, positions
		for(Particle particle:mParticleList){
			particle.update(this);
		}
		
		timer.update();
		timer.println("Update forces");
		
		//Polygonise surface
		mMCGrid.march();

		
		//MCCube cube = mMCGrid.getCubes().get(111);
		//Vector<MCCube> cubes = cube.getCubesInRange(3);
		
		//Debug.println("cubes withing range 3 of cube 555: " + cubes.size(),Debug.MAX_DEBUG);
//		for(MCCube c:cubes){
//			System.out.print(c.getId() + " ");
//		}
	}
	
	public Particle getParticle(int particleID){
		Particle particle = mParticleList.get(particleID);
		return particle;
		
	}
	
	public Vector<Particle> getParticleList(){
		return mParticleList;
	}
	
	public Vector4f getColor(){
		return mColor;
	}
	
	public void setColor(Vector4f color){
		mColor = color;
	}
	
	public int getNumberOfParticles(){
		return mNumberOfParticles;
	}
	
	public float getParticleMass(){
		return mParticleMass;
	}
	
	public float getParticleSize(){
		return mParticleSize;
	}
	
	public void setNumberOfParticles(int numberOfParticles){
		mNumberOfParticles = numberOfParticles;
	}
	
	public void setParticleMass(float particleMass){
		mParticleMass = particleMass;
	}
	
	public Boundaries getBoundaries(){
		return mBoundaries;
	}
	
	public MCGrid getGrid(){
		return mMCGrid;
	}
	
	public int drawMode(){
		return mDrawMode;
	}
	
	/**
	 * Set the drawmode
	 * Allowed values {@link Liquid.DRAW_PARTICLES, Liquid.DRAW_SURFACE, Liquid.DRAW_GRID}
	 * @param drawMode
	 */
	public void setDrawMode(int drawMode){
		mDrawMode = drawMode;
	}
	
	public class Boundaries{
		public String type;
		//Liquid bounds
		public float xLow;
		public float xHigh;
		public float yLow;
		public float yHigh;
		public float zLow;
		public float zHigh;
		
		private boolean mSideConstraintsOn = true;
		
		public Boundaries(float xL,float xH,float yL,float yH,float zL,float zH){
			type = "box";
			xLow = xL;
			xHigh = xH;
			yLow = yL;
			yHigh = yH;
			zLow = zL;
			zHigh = zH;
		}
		
		public Boundaries(float size){
			type = "box";
			xLow = 0;
			xHigh = size;
			yLow = 0;
			yHigh = size;
			zLow = 0;
			zHigh = size;
		}
		
		public boolean isSideConstraintsOn(){
			return mSideConstraintsOn;
		}
		
		public void setSideConstraintsOn(boolean b){
			mSideConstraintsOn = b;
		}
	}
}
