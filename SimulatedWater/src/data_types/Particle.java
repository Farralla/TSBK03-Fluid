package data_types;

import static org.lwjgl.util.vector.Vector3f.sub;

import java.util.Vector;

import marching_cubes.CubeInterface;
import marching_cubes.MCCube;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Utils.Kernel;
import Utils.MathUtils;
import data_types.Liquid.Boundaries;

/*For each time step: For each time step:
1. 1. Find neighbors to each particle and store in a Find neighbors to each particle and store in a 
list list
2. 2. Calculate density for each particle Calculate density for each particle
3. 3. Calculate pressure for each particle Calculate pressure for each particle
4. 4. Calculate all type of accelerations for each Calculate all type of accelerations for each 
particle, and sum it up particle, and sum it up
5. 5. Find new velocities and positions by using the Find new velocities and positions by using the 
same integration method as before… */

/**
 * The Particles which make up the liquid system
 * @author Martin
 *
 */
public class Particle implements ParticleInterface{
	//time stamps
	private double timeStamp;
	private double dT = 0.0007;
	//Member variables
	private float mMass;
	private float mDensity;
	private float mPressure;
	private Vector3f mVelocity;
	private Vector3f mAcceleration;
	private Vector3f mPosition;
	private Vector3f mForce;
	
	//Tuning variables
	private Vector4f mColor;
	private float mK; //Gas constant
	private float mRho; //Initial density
	private float mH; //radius of influence
	private float mEta; //Viscosity constant
	private static final float g = 9.81f; //Gravity
	private float mSigma; //colorfield constant
	private float mCFThresh; //colorfield gradient length threshold
	
	//Neighbouring particles
	private Vector<Particle> mNeighborList;
	
	//In which cube is this particle located
	CubeInterface mInCube;
	
	/**
	 * Constructor
	 */
	public Particle(){
		mVelocity.set(0,0,0);
		mAcceleration.set(0,0,0);
		mPosition = new Vector3f(0,0,0);
		mColor = new Vector4f(1,0,0,1);
		mNeighborList =  new Vector<Particle>();
		timeStamp = System.currentTimeMillis();
		//Tuning
		mH =  1;
		mK = 1; // m/s in water, probably to high for Euler integration
		mRho = 1000; // number of particles?
		mEta = 1000; //1,002 mPa*s
		mMass = 0.01f;
	}
	
	/**
	 * Constructor with
	 * @param mass
	 * @param position
	 */
	public Particle(Liquid liquid, Vector3f position){
		//Tuning
		mH =  0.007f; //cm ----> about 17 particles in radius
		mK = 1000f; // speed of sound^2 = 5^2 can be set higher
		mRho = 1000f; // kg/cm^3
		mEta = 20000f; //1,002 mPa*s
		mSigma = 5000f;
		mCFThresh = 100f;
		mMass = 0.001f;
		
		mDensity = mRho;
		mPressure = 0;
		mVelocity = new Vector3f(0,0,0);
		mAcceleration = new Vector3f(0,0,0);
		mPosition = new Vector3f(position);
		mForce = new Vector3f(0,0,0);
		mColor = new Vector4f(1,0,0,1);
		mNeighborList =  new Vector<Particle>();
		timeStamp = System.currentTimeMillis();
		initialInCube(liquid.getGrid().getCubes());
	}
	
	/**
	 * 	For each time step: For each time step:
		1. Find neighbors to each particle and store in a Find neighbors to each particle and store in a 
		list list
		2. Calculate density for each particle
		3. Calculate pressure for each particle
	 * @param liquid
	 */
	public void updateDensityAndPressure(Liquid liquid){

		updateNeighborList(liquid.getParticleList());
		//System.out.println(mNeighborList.size());
		calcDensity();
		calcPressure();
	}
	
	/**

	
	/**
	 * 4 Calculate forces from pressure, viscosity and surface tension
	 * 5 Calculate all type of accelerations for each particle, and sum it up particle,
	 * 6 Find new velocities and positions
	 * @param liquid
	 */
	public void update(Liquid liquid){
		//Add up pressure force, viscosity force and gravity force
			Vector3f pressureForce = new Vector3f(0,0,0);
			Vector3f viscosityForce = new Vector3f(0,0,0);
			Vector3f surfaceTensionForce = new Vector3f(0,0,0);
			Vector3f cS_grad = new Vector3f(0,0,0);
			float cS_lap = 0;
			//System.out.println()
			//Add contributions from all neighbors
			for(Particle neighbor:mNeighborList){
				pressureForce = Vector3f.add(pressureForce, pressureForce(neighbor), null);
				viscosityForce = Vector3f.add(viscosityForce, viscosityForce(neighbor), null);
				//cS_lap += colorField(neighbor);
				//cS_grad = Vector3f.add(cS_grad, colorFieldGradient(neighbor), null);
			}
			
			//Update surface Tension force if the gradient is large enough
//			float gradientLength = cS_grad.length();
//			//System.out.println("Gradient length: " + gradientLength);
//			if(gradientLength >= mCFThresh){
//				surfaceTensionForce = (Vector3f) cS_grad.scale(-mSigma*cS_lap/gradientLength);
//			}
//			
			//Update total force
			mForce = Vector3f.add(mForce,pressureForce,null);
			mForce = Vector3f.add(mForce,viscosityForce,null);
			//mForce = Vector3f.add(mForce,surfaceTensionForce,null);
			//System.out.println("pressureForce: " + pressureForce.length());
			//System.out.println("viscosityForce: " + viscosityForce.length());
			//System.out.println("surfaceTensionForce: " + surfaceTensionForce.length());
			
			
			/*Can add interacttive forces here
			*
			* TODO INTERACTIVE FORCES!
			* 
			*/
			//dT = 0.001*(System.currentTimeMillis() - timeStamp);
			//System.out.println(dT);
			
			
			timeStamp = System.currentTimeMillis();
			
			//Update acceleration and velocity
			mAcceleration = Vector3f.add(
					(Vector3f) mForce.scale((float) (dT/mDensity)),
					gravity(),
					null);
			
			mVelocity = Vector3f.add(mVelocity,
					(Vector3f) mAcceleration.scale((float) dT),
					null);
			
			//System.out.println(mForce);
			mPosition = Vector3f.add(mPosition, MathUtils.scalarMultVec3(mVelocity,(float) dT), null);
			
			
			mForce.scale(0);
			mAcceleration.scale(0);

			checkBoundaries(liquid);
			
			//Update which cube the particle is in
			updateInCube();
	}
	
	/**
	 * Adds all particles within radius h to neighborlist
	 * @param listOfAllParticles Vector of all particles in the liquid
	 * @param h neighboring radius
	 */
	public void updateNeighborList(Vector<Particle> listOfAllParticles){
		mNeighborList.clear();
		for(Particle particle:listOfAllParticles){
			float distance = distance(particle);
			if(distance <= mH){
				mNeighborList.add(particle);
			}
		}

	}
	
	public CubeInterface initialInCube(Vector<MCCube> vector){
		CubeInterface resultCube = null;
		for(CubeInterface cube:vector){
			if(cube.containsParticle(this)){
				resultCube = cube;
				mInCube = cube;
				resultCube.addParticle(this);
				break;
			}
		}
		return resultCube;
	}
	
	public CubeInterface updateInCube(){
		if(mInCube.containsParticle(this)){
			return mInCube;
		}
		else{
			for(CubeInterface cube:mInCube.getNeighbours().values()){
				if(cube.containsParticle(this)){
					mInCube.removeParticle(this);
					cube.addParticle(this);
					mInCube = cube;
					return mInCube;
				}
			}
		}
		return mInCube;
	}
	

	/**
	 * Calculates density of particle from neighboring particles
	 * @return (float) density
	 */
	private double calcDensity(){
		float temp = 0;	
		//Addition of all other particles
		for(Particle neighbor:mNeighborList){
			Vector3f r_vec = new Vector3f();
			r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
			temp += neighbor.getMass()*Kernel.W_poly6(r_vec, mH);
		}
		mDensity = temp;
		return temp;
	}
	 
	/**
	 * Calculates pressure of particle from density
	 * @return
	 */
	private double calcPressure(){
		mPressure = mK*(mDensity-mRho);
		//System.out.println(mPressure);
		return mPressure;
	}
	
	/**
	 * Calculates pressure force of particle from neighboring particle
	 * Note the "-" before temp term
	 * @return
	 */
	private Vector3f pressureForce(Particle neighbor){
		Vector3f pressureForce = new Vector3f();
		Vector3f r_vec = new Vector3f();
		r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
		float temp = -(neighbor.getMass()*(neighbor.getPressure() + mPressure)/(2*neighbor.getDensity()));
		pressureForce = Kernel.GradW_pressure(r_vec, mH);
		pressureForce.scale(temp);
		return pressureForce;
	}
	
	/**
	 * Calculates viscosity force of particle from neighboring particle
	 * @return
	 */
	private Vector3f viscosityForce(Particle neighbor){
		Vector3f r_vec = new Vector3f();
		Vector3f v_vec = new Vector3f();
		r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
		
		v_vec = Vector3f.sub(neighbor.getVelocity(), mVelocity, null);
		Vector3f viscosityForce = v_vec;
		viscosityForce.scale(mEta*neighbor.getMass()/neighbor.getDensity()*Kernel.LapW_viscosity(r_vec, mH));
		return viscosityForce;
	}
	
	/**
	 * Calculates the color field addition from neighbor
	 * @param neighbor
	 * @return
	 */
	private float colorField(Particle neighbor){
		Vector3f r_vec = new Vector3f();
		r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
		float W = neighbor.getMass() / neighbor.getDensity() * Kernel.LapW_poly6(r_vec, mH);
		return W;
	}
	
	/**
	 * Calculates the color field gradient addition from neighbor
	 * @param neighbor
	 * @return
	 */
	private Vector3f colorFieldGradient(Particle neighbor){
		Vector3f r_vec = new Vector3f();
		Vector3f gradW = new Vector3f();
		r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
		gradW = Kernel.GradW_poly6(r_vec, mH);
		float W = neighbor.getMass() / neighbor.getDensity();
		gradW.scale(W);
		return gradW;
	}
	
	/**
	 * Gravity force in negative y-axis
	 * @return
	 */
	private Vector3f gravity(){
		return new Vector3f(0,-g,0);
	}
	
	private void checkBoundaries(Liquid liquid){
		Boundaries b = liquid.getBoundaries();
		if(mPosition.x<b.xLow){
			mPosition.x = b.xLow;
			mVelocity.x = 0;
			mForce.x = 10000f;
		}
		else if(mPosition.x>b.xHigh){
			mPosition.x = b.xHigh;
			mVelocity.x = 0;
			mForce.x = -10000f;
		}
		
		if(mPosition.y<b.yLow){
			mPosition.y = b.yLow;
			mVelocity.y = 0;
			//mForce.y = 1f;
		}
		else if(mPosition.y>b.yHigh){
			mPosition.y = b.yHigh;
			mVelocity.y = 0;
			mForce.y = -100f;
		}
		
		if(mPosition.z<b.zLow){
			mPosition.z = b.zLow;
			mVelocity.z = 0;
			mForce.z = 10000f;
		}
		else if(mPosition.z>b.zHigh){
			mPosition.z = b.zHigh;
			mVelocity.z = 0;
			mForce.z = -10000f;
		}
	}
	
	/**
 	* Calculates distance to neighboring particle
 	* @param neighbor Neighboring particle
 	* @return distance to neighboring particle
 	*/
	public float distance(Particle neighbor){
		Vector3f vec = new Vector3f();
		vec = sub(mPosition, neighbor.getPosition(), null);
		return vec.length();
	}
	
	/**
	 * Calculate vector from neighbor to this particle
	 * @param neighbor
	 * @return
	 */
	public Vector3f vecToNeighbor(Particle neighbor){
		Vector3f vec = sub(neighbor.getPosition(),mPosition, null);
		return vec;
	}
	
	//Get functions
	public float getMass(){
		return mMass;
	}
	
	public float getDensity(){
		return mDensity;
	}
	
	public float getPressure(){
		return mDensity;
	}
	
	public Vector3f getVelocity(){
		return mVelocity;
	}
	
	public Vector3f getPosition(){
		return mPosition;
	}
	
	public Vector3f getForce(){
		return mForce;
	}
	
	public Vector4f getColor(){
		return mColor;
	}
	
	//Set functions
	public void setMass(float mass){
		mMass = mass;
	}
	
	public void setDensity(float density){
		mDensity = density;
	}
	
	public void setPressure(float pressure){
		mDensity = pressure;
	}
	
	public void setVelocity(Vector3f velocity){
		mVelocity = velocity;
	}
	
	public void setPosition(Vector3f position){
		mPosition = position;
	}
	
	public void setForce(Vector3f force){
		mForce = force;
	}
	
	public void setColor(Vector4f color){
		mColor = color;
	}
}
