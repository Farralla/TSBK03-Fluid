package data_types;

import static org.lwjgl.util.vector.Vector3f.sub;

import java.util.Vector;
import marching_cubes.MCCube;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Utils.Debug;
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
 * 
 * @author Martin
 *
 */
public class Particle {
	private static final float MAX_FORCE = 5000000;
	private static final float MAX_ACCELERATION = 20;
	private static final float MAX_VELOCITY = 0.5f;

	// time stamps
	private double timeStamp;
	private double dT = 0.02;

	// Member variables
	private float mMass;
	private float mDensity;
	private float mPressure;
	private Vector3f mVelocity;
	private Vector3f mAcceleration;
	private Vector3f mPosition;
	private Vector3f mForce;

	// Tuning variables
	private Vector4f mColor;
	private float mK; // Gas constant
	private float mRho; // Initial density
	private float mH; // radius of influence
	private float mEta; // Viscosity constant
	private static final float g = 2.5f; // Gravity
	private float mSigma; // colorfield constant
	private float mCFThresh; // colorfield gradient length threshold

	// Kernel
	private Kernel mKernel;

	// Neighbouring particles
	private Vector<Particle> mNeighborList;

	// In which cube is this particle located
	MCCube mInCube;

	/**
	 * Constructor
	 */
	public Particle() {
		mVelocity.set(0, 0, 0);
		mAcceleration.set(0, 0, 0);
		mPosition = new Vector3f(0, 0, 0);
		mColor = new Vector4f(1, 0, 0, 1);
		mNeighborList = new Vector<Particle>();
		timeStamp = System.currentTimeMillis();
		// Tuning
		mH = 1;
		mK = 1; // m/s in water, probably to high for Euler integration
		mRho = 1000; // number of particles?
		mEta = 1000; // 1,002 mPa*s
		mMass = 0.01f;
	}

	/**
	 * Constructor with
	 * 
	 * @param mass
	 * @param position
	 */
	public Particle(Liquid liquid, Vector3f position) {
		// Tuning
		mH = 0.05f; // cm ----> about 17 particles in radius
		mK = 200f; // speed of sound^2 = 5^2 can be set higher
		mRho = 1000f; // kg/cm^3
		mEta = 100f; // 1,002 mPa*s
		mSigma = 200f;
		mCFThresh = 10f;
		mMass = 0.05f;

		mDensity = mRho;
		mPressure = 0;
		mVelocity = new Vector3f(0, 0, 0);
		mAcceleration = new Vector3f(0, 0, 0);
		mPosition = new Vector3f(position);
		mForce = new Vector3f(0, 0, 0);
		mColor = new Vector4f(1, 0, 0, 1);
		mNeighborList = new Vector<Particle>();
		timeStamp = System.currentTimeMillis();
		initialInCube(liquid.getGrid().getCubes());
		mKernel = new Kernel(mH);
	}

	/**
	 * For each time step: For each time step: 1. Find neighbors to each
	 * particle and store in a Find neighbors to each particle and store in a
	 * list list 2. Calculate density for each particle 3. Calculate pressure
	 * for each particle
	 * 
	 * @param liquid
	 */
	public void updateDensityAndPressure(Liquid liquid) {
		Vector<MCCube> nearbyCubes = mInCube.getCubesInRange(3);
		Vector<Particle> nearbyParticles = new Vector<Particle>();
		for (MCCube cube : nearbyCubes) {
			nearbyParticles.addAll(cube.getParticles());
		}
		updateNeighborList(nearbyParticles);

		calcDensity();
		calcPressure();
	}

	/**
	 * /** 4 Calculate forces from pressure, viscosity and surface tension 5
	 * Calculate all type of accelerations for each particle, and sum it up
	 * particle, 6 Find new velocities and positions
	 * 
	 * @param liquid
	 */
	public void update(Liquid liquid) {
		// CalcObject calcPressure = new
		// CalcObject(Calculator.CALC_PRESSURE_FORCE);
		// CalcObject calcViscosity = new
		// CalcObject(Calculator.CALC_VISCOSITY_FORCE);

		// Add up pressure force, viscosity force and gravity force
		Vector3f pressureForce = new Vector3f(0, 0, 0);
		Vector3f viscosityForce = new Vector3f(0, 0, 0);
		Vector3f surfaceTensionForce = new Vector3f(0, 0, 0);
		Vector3f cS_grad = new Vector3f(0, 0, 0);
		float cS_lap = 0;

		// Add contributions from all neighbors

		// (new Thread(new Calculator(calcPressure) {
		// })).start();
		//
		// (new Thread(new Calculator(calcViscosity) {
		// })).start();
		//
		// while(!calcPressure.finished.booleanValue() ||
		// !calcViscosity.finished.booleanValue()){
		// //Debug.println(calcPressure.finished.toString() +
		// calcViscosity.finished.toString(), Debug.MAX_DEBUG);
		// }

		for (Particle neighbor : mNeighborList) {
			pressureForce = Vector3f.add(pressureForce, pressureForce(neighbor), null);
			viscosityForce = Vector3f.add(viscosityForce, viscosityForce(neighbor), null);
			cS_lap += colorField(neighbor);
			cS_grad = Vector3f.add(cS_grad, colorFieldGradient(neighbor), null);
		}

		// Update surface Tension force if the gradient is large enough
		float gradientLength = cS_grad.length();
		// System.out.println("Gradient length: " + gradientLength);
		if (gradientLength >= mCFThresh) {
			surfaceTensionForce = (Vector3f) cS_grad.scale(-mSigma * cS_lap / gradientLength);
		}
		//
		// Update total force
		mForce = Vector3f.add(mForce, pressureForce, null);
		mForce = Vector3f.add(mForce, viscosityForce, null);
		mForce = Vector3f.add(mForce, surfaceTensionForce, null);

		if (mForce.y > MAX_FORCE) {
			mForce.y = MAX_FORCE;
		}

		/*
		 * Can add interacttive forces here
		 * 
		 * TODO INTERACTIVE FORCES!
		 */
		// dT = 0.001*(System.currentTimeMillis() - timeStamp);
		// System.out.println(dT);

		// timeStamp = System.currentTimeMillis();

		// Update acceleration and velocity
		mAcceleration = Vector3f.add(
				(Vector3f) mForce.scale((float) (dT / mDensity)),
				gravity(),
				null);

		// if(mAcceleration.length() > MAX_ACCELERATION){
		// mAcceleration.normalise().scale(MAX_ACCELERATION);
		// }

		float kA = 1 - mAcceleration.length() * 0.01f;
		mAcceleration.scale(kA);

		mVelocity = Vector3f.add(mVelocity,
				(Vector3f) mAcceleration.scale((float) dT),
				null);

		float k = 1 - mVelocity.length() * 0.15f;
		mVelocity.scale(k);

		// if(mVelocity.length() > MAX_VELOCITY){
		// //Debug.println("VELOCITY: " + mVelocity.length(),Debug.MAX_DEBUG);
		// mVelocity.normalise().scale(MAX_VELOCITY);
		// }

		// System.out.println(mForce);
		mPosition = Vector3f.add(mPosition, MathUtils.scalarMultVec3(mVelocity, (float) dT), null);

		mForce.set(0, 0, 0);
		mAcceleration.set(0, 0, 0);

		checkBoundaries(liquid);

		// Update which cube the particle is in
		updateInCube();

		// Add density value to nearby grid cubes
		updateNearbyCubes();
	}

	public void updateNearbyCubes() {
		Vector<MCCube> nearbyCubes = mInCube.getCubesInRange(5);
		for (MCCube cube : nearbyCubes) {

			for (int i = 0; i < 8; i++) {
				Vector3f rvec = Vector3f.sub(mPosition, cube.getPosition(i), null);
				if (rvec.length() < mH) {
					cube.addToValue(i, 0.00001f * mDensity * mKernel.W_poly6(rvec));
				}
			}
		}

	}

	/**
	 * Adds all particles within radius h to neighbor list
	 * 
	 * @param listOfParticles Vector of particles in nearby cubes
	 * @param h neighboring radius
	 */
	public void updateNeighborList(Vector<Particle> listOfParticles) {
		mNeighborList.removeAllElements();
		for (Particle particle : listOfParticles) {
			float distance = distance(particle);
			if (distance <= mH) {
				mNeighborList.add(particle);
			}
		}
	}

	/**
	 * Determine which cube the particle is in from particle position Loops
	 * through all cubes
	 * 
	 * @param vector
	 *            Vector of all cubes
	 * @return cube which the particle is in
	 */
	public MCCube initialInCube(Vector<MCCube> vector) {
		MCCube resultCube = null;
		for (MCCube cube : vector) {
			if (cube.containsParticle(this)) {
				resultCube = cube;
				mInCube = cube;
				resultCube.addParticle(this);
				break;
			}
		}
		return resultCube;
	}

	/**
	 * Determine which cube the particle is in from particle position Loops
	 * through the neighbours of the current cube
	 * 
	 * @return new cube chich the particle is in
	 */
	public void updateInCube() {
		if (mInCube.containsParticle(this)) {
			return;
		}
		else {
			Vector<MCCube> nearbyCubes = mInCube.getCubesInRange(4);
			for (MCCube cube : nearbyCubes) {
				if (cube.containsParticle(this)) {
					mInCube.removeParticle(this);
					cube.addParticle(this);
					mInCube = cube;
					break;
				}
			}
		}
	}

	/**
	 * Calculates density of particle from neighboring particles
	 * 
	 * @return (float) density
	 */
	private double calcDensity() {
		float temp = 0;
		// Addition of all other particles
		for (Particle neighbor : mNeighborList) {
			Vector3f r_vec = new Vector3f();
			r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
			temp += neighbor.getMass() * mKernel.W_poly6(r_vec);
		}
		mDensity = temp;
		return temp;
	}

	/**
	 * Calculates pressure of particle from density
	 * 
	 * @return
	 */
	private double calcPressure() {
		mPressure = mK * (mDensity - mRho);
		return mPressure;
	}

	/**
	 * Calculates pressure force of particle from neighboring particle Note the
	 * TODO What is correct: "-" before temp term or not?
	 * 
	 * @return
	 */
	private Vector3f pressureForce(Particle neighbor) {
		Vector3f pressureForce = new Vector3f();
		Vector3f r_vec = new Vector3f();
		r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
		float temp = -(neighbor.getMass() * (neighbor.getPressure() + mPressure) / (2 * neighbor.getDensity()));
		pressureForce = mKernel.GradW_pressure(r_vec);
		pressureForce.scale(temp);
		return pressureForce;
	}

	/**
	 * Calculates viscosity force of particle from neighboring particle
	 * 
	 * @return
	 */
	private Vector3f viscosityForce(Particle neighbor) {
		Vector3f r_vec = new Vector3f();
		Vector3f v_vec = new Vector3f();
		r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);

		v_vec = Vector3f.sub(neighbor.getVelocity(), mVelocity, null);
		Vector3f viscosityForce = v_vec;
		viscosityForce.scale(mEta * neighbor.getMass() / neighbor.getDensity() * mKernel.LapW_viscosity(r_vec));
		return viscosityForce;
	}

	/**
	 * Calculates the color field addition from neighbor
	 * 
	 * @param neighbor
	 * @return
	 */
	private float colorField(Particle neighbor) {
		Vector3f r_vec = new Vector3f();
		r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
		float W = neighbor.getMass() / neighbor.getDensity() * mKernel.LapW_poly6(r_vec);
		return W;
	}

	/**
	 * Calculates the color field gradient addition from neighbor
	 * 
	 * @param neighbor
	 * @return
	 */
	private Vector3f colorFieldGradient(Particle neighbor) {
		Vector3f r_vec = new Vector3f();
		Vector3f gradW = new Vector3f();
		r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
		gradW = mKernel.GradW_poly6(r_vec);
		float W = neighbor.getMass() / neighbor.getDensity();
		gradW.scale(W);
		return gradW;
	}

	/**
	 * Gravity force in negative y-axis
	 * 
	 * @return
	 */
	private Vector3f gravity() {
		return new Vector3f(0, -g, 0);
	}

	private void checkBoundaries(Liquid liquid) {
		Boundaries b = liquid.getBoundaries();
		float k = 0.9f;
		if (mPosition.x < b.xLow) {
			mPosition.x = b.xLow;
			mVelocity.x = -k * mVelocity.x;
			float kV = 1 - mVelocity.length() * 0.5f;
			mVelocity.scale(kV);
			mForce.x = 1000f;
		}
		else if (mPosition.x > b.xHigh) {
			mPosition.x = b.xHigh;
			mVelocity.x = -k * mVelocity.x;
			float kV = 1 - mVelocity.length() * 0.5f;
			mVelocity.scale(kV);
			mForce.x = -1000f;
		}

		if (mPosition.y < b.yLow) {
			mPosition.y = b.yLow;
			mVelocity.y = 0;
			float kV = 1 - mVelocity.length() * 0.5f;
			mVelocity.scale(kV);
			mVelocity.x = 0;
			// mForce.y = 1000f;
		}
		else if (mPosition.y > b.yHigh) {
			mPosition.y = b.yHigh;
			mVelocity.y = 0;
			float kV = 1 - mVelocity.length() * 0.5f;
			mVelocity.scale(kV);
			mForce.y = -1000f;
		}

		if (mPosition.z < b.zLow) {
			mPosition.z = b.zLow;
			mVelocity.z = -k * mVelocity.z;
			float kV = 1 - mVelocity.length() * 0.5f;
			mVelocity.scale(kV);
			mForce.z = 1000f;
		}
		else if (mPosition.z > b.zHigh) {
			mPosition.z = b.zHigh;
			mVelocity.z = -k * mVelocity.z;
			float kV = 1 - mVelocity.length() * 0.5f;
			mVelocity.scale(kV);
			mForce.z = -1000f;
		}
	}

	/**
	 * Calculates distance to neighboring particle
	 * 
	 * @param neighbor
	 *            Neighboring particle
	 * @return distance to neighboring particle
	 */
	public float distance(Particle neighbor) {
		Vector3f vec = new Vector3f();
		vec = sub(mPosition, neighbor.getPosition(), null);
		return vec.length();
	}

	/**
	 * Calculate vector from neighbor to this particle
	 * 
	 * @param neighbor
	 * @return
	 */
	public Vector3f vecToNeighbor(Particle neighbor) {
		Vector3f vec = sub(neighbor.getPosition(), mPosition, null);
		return vec;
	}

	// Get functions
	public float getMass() {
		return mMass;
	}

	public float getDensity() {
		return mDensity;
	}

	public float getPressure() {
		return mDensity;
	}

	public Vector3f getVelocity() {
		return mVelocity;
	}

	public Vector3f getPosition() {
		return mPosition;
	}

	public Vector3f getForce() {
		return mForce;
	}

	public Vector4f getColor() {
		return mColor;
	}

	// Set functions
	public void setMass(float mass) {
		mMass = mass;
	}

	public void setDensity(float density) {
		mDensity = density;
	}

	public void setPressure(float pressure) {
		mDensity = pressure;
	}

	public void setVelocity(Vector3f velocity) {
		mVelocity = velocity;
	}

	public void setPosition(Vector3f position) {
		mPosition = position;
	}

	public void setForce(Vector3f force) {
		mForce = force;
	}

	public void setColor(Vector4f color) {
		mColor = color;
	}

	public class CalcObject {
		public Vector3f calcVector;
		public int calcType;
		public Boolean finished;;

		CalcObject(int calcType) {
			this.calcType = calcType;
			this.finished = new Boolean(false);
			calcVector = new Vector3f();
		}

		public void setFinished(boolean b) {
			this.finished = b;
		}
	}

	public class Calculator implements Runnable {
		public static final int CALC_PRESSURE_FORCE = 0;
		public static final int CALC_VISCOSITY_FORCE = 1;

		private CalcObject mCalcObject;

		/**
		 * Constructor
		 * 
		 * @param v
		 *            result is put here
		 * @param calcType
		 *            What kind of operation
		 * @param finished
		 *            Signal flag to indicate that the calculation is finished
		 */
		public Calculator(CalcObject calcObject) {
			mCalcObject = calcObject;
		}

		@Override
		public void run() {

			switch (mCalcObject.calcType) {
			case CALC_PRESSURE_FORCE:
				calcPressureForce();
				break;
			case CALC_VISCOSITY_FORCE:
				calcViscosityForce();
				break;
			}

			mCalcObject.setFinished(true);
			// synchronized(mFinished){
			// mFinished.notifyAll();
			// }

		}

		private void calcPressureForce() {
			Vector3f pressureForce = new Vector3f(0, 0, 0);
			for (Particle neighbor : mNeighborList) {
				pressureForce = Vector3f.add(pressureForce, pressureForce(neighbor), null);
			}
			mCalcObject.calcVector = pressureForce;
		}

		private void calcViscosityForce() {
			Vector3f viscosityForce = new Vector3f(0, 0, 0);
			for (Particle neighbor : mNeighborList) {
				viscosityForce = Vector3f.add(viscosityForce, viscosityForce(neighbor), null);
			}
			mCalcObject.calcVector = viscosityForce;
		}
	}
}
