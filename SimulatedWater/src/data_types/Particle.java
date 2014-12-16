package data_types;

import static org.lwjgl.util.vector.Vector3f.sub;

import java.util.Vector;

import marching_cubes.MCCube;
import marching_cubes.MCGrid;
import marching_cubes.MCVoxel;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Utils.Debug;
import Utils.Kernel;
import Utils.MathUtils;
import Utils.Timer;
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
	private static final float MAX_FORCE = 1000;
	private static final float MAX_ACCELERATION = 40;
	private static final float MAX_VELOCITY = 1f;

	// time stamps
	private double timeStamp;

	// Member variables
	private float mDensity;
	private float mPressure;
	private Vector3f mVelocity;
	private Vector3f mAcceleration;
	private Vector3f mPosition;
	private Vector3f mForce;

	// Tuning variables same for all particles
	private static Vector4f mColor;
	private static float mK; // Gas constant
	private static float mRho; // Initial density
	private static float mH; // radius of influence
	private static float mEta; // Viscosity constant
	private static float mSigma; // colorfield constant
	private static float mCFThresh; // colorfield gradient length threshold
	private static float mMass; // The mass of a particle
	private static Kernel mKernel; // Kernel used in forces calculations

	private boolean mInCorner = false;

	// Neighbouring particles
	private Vector<Particle> mNeighborList;

	// In which cube is this particle located
	private MCCube mInCube;

	public static void setValues() {
		// Tuning
		mH = 0.03f; // cm ----> about 17 particles in radius
		mK = 0.5f; //
		mRho = 0.00001f; // kg/cm^3
		mEta = 0.001f; // 1,002 mPa*s
		mSigma = 0.001f;
		mCFThresh = 30f;
		mMass = 0.0004f;
		mColor = new Vector4f(1, 0, 0, 1);
		mKernel = new Kernel(mH);
	}

	/**
	 * Constructor with
	 * 
	 * @param mass
	 * @param position
	 */
	public Particle(Liquid liquid, Vector3f position) {
		mDensity = mRho;
		mPressure = 0;
		mVelocity = new Vector3f(0, 0, 0);
		mAcceleration = new Vector3f(0, 0, 0);
		mPosition = new Vector3f(position);
		mForce = new Vector3f(0, 0, 0);
		mNeighborList = new Vector<Particle>();
		timeStamp = System.currentTimeMillis();
		initialInCube(liquid.getGrid().getCubes());
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
		Vector<MCCube> nearbyCubes = mInCube.getNearbyCubes();
		Vector<Particle> nearbyParticles = new Vector<Particle>();

		for (MCCube cube : nearbyCubes) {
			nearbyParticles.addAll(cube.getParticles());
		}
		updateNeighborList(nearbyParticles);
		if (!mInCorner) {
			calcDensity();
		} else {
			calcDensity();
			mInCorner = false;
		}
		calcPressure();
	}

	/**
	 * /** 4 Calculate forces from pressure, viscosity and surface tension 5
	 * Calculate all type of accelerations for each particle, and sum it up
	 * particle, 6 Find new velocities and positions
	 * 
	 * @param liquid
	 */
	public void updateForces(Liquid liquid) {
		// Add up pressure force, viscosity force and gravity force
		Vector3f pressureForce = new Vector3f(0, 0, 0);
		Vector3f viscosityForce = new Vector3f(0, 0, 0);
		Vector3f surfaceTensionForce = new Vector3f(0, 0, 0);
		Vector3f cS_grad = new Vector3f(0, 0, 0);
		float cS_lap = 0;
		// Add contributions from all neighbors
		for (Particle neighbor : mNeighborList) {
			pressureForce = Vector3f.add(pressureForce, pressureForce(neighbor), null);
			viscosityForce = Vector3f.add(viscosityForce, viscosityForce(neighbor), null);
			cS_lap += colorField(neighbor);
			cS_grad = Vector3f.add(cS_grad, colorFieldGradient(neighbor), null);
		}
		// Update surface Tension force if the gradient is large enough
		float gradientLength = cS_grad.length();
		if (gradientLength >= mCFThresh) {
			surfaceTensionForce = (Vector3f) cS_grad.scale(-mSigma * cS_lap / gradientLength);
		}
		// Update total force
		mForce = Vector3f.add(mForce, pressureForce, null);
		mForce = Vector3f.add(mForce, viscosityForce, null);
		mForce = Vector3f.add(mForce, surfaceTensionForce, null);
		//
		// if (mForce.y > MAX_FORCE) {
		// mForce.y = MAX_FORCE;
		// }
		/*
		 * Can add interacttive forces here
		 * 
		 * TODO INTERACTIVE FORCES!
		 */
		// dT = 0.001*(System.currentTimeMillis() - timeStamp);
		// System.out.println(dT);

		// Update acceleration and velocity
		mAcceleration = Vector3f.add(
				(Vector3f) mForce.scale((float) (Liquid.dT / mDensity)),
				Liquid.gravity(),
				null);
		// if (mAcceleration.length() > MAX_ACCELERATION) {
		// mAcceleration.normalise().scale(MAX_ACCELERATION);
		// }

		// float kA = 1 - mAcceleration.length() * 0.1f;
		// mAcceleration.scale(kA);

	}

	public void update(Liquid liquid) {

		checkBoundaries(liquid);
		checkCollisions(liquid);
		synchronized (this) {
			mVelocity = Vector3f.add(mVelocity,
					(Vector3f) mAcceleration.scale((float) Liquid.dT),
					null);
		}
		// float k = 1 - mVelocity.length() * 0.01f;
		// mVelocity.scale(k);

		// if (mVelocity.length() > MAX_VELOCITY) {
		// mVelocity.normalise().scale(MAX_VELOCITY);
		// }

		synchronized (this) {
			mPosition = Vector3f.add(mPosition, (Vector3f) mVelocity.scale((float) Liquid.dT), null);
		}

		mForce.set(0, 0, 0);
		mAcceleration.set(0, 0, 0);
		// Update which cube the particle is in
		updateInCube(liquid);
		// Add density value to nearby grid cubes
		updateNearbyCubes();
	}

	private void checkCollisions(Liquid liquid) {
		Vector3f totForce = new Vector3f(0, 0, 0);
		for (CollidableSphere c : liquid.getCollidables()) {
			Vector3f rvec = Vector3f.sub(mPosition, c.getPosition(), null);
			float distance = rvec.length() - c.getRadius();
			if (distance <= mH) {
				rvec.normalise().scale(distance);
				totForce = (handleCollision(rvec, c.getMass(), c.getDensity()));
				c.addForce((Vector3f) totForce.scale(-1));
				// mVelocity.scale(0.6f);
			}
		}
		mForce = Vector3f.add(mForce, totForce, null);

	}

	private Vector3f handleCollision(Vector3f rvec, float mass, float density) {
		Vector3f pressureForce = new Vector3f(0, 0, 0);
		Vector3f viscosityForce = new Vector3f(0, 0, 0);
		pressureForce = pressureForce(rvec, mass, density);
		viscosityForce = viscosityForce(rvec, mass, density);
		Vector3f totForce = Vector3f.add(pressureForce, viscosityForce, null);
		return totForce;
	}

	public void updateNearbyCubes() {
		Vector<MCVoxel> nearbyVoxels = mInCube.getNearbyVoxels();
		for (MCVoxel voxel : nearbyVoxels) {

			Vector3f rvec = Vector3f.sub(voxel.getPosition(), mPosition, null);
			if (rvec.length() < mH) {
				voxel.addValue(0.00001f * mDensity * mKernel.W_poly6(rvec));
				// float s = 1 - rvec.length() / mH;
				// Vector3f val = (Vector3f) rvec.scale(s);
				// voxel.addToValue(i, val);
			}
		}
	}

	/**
	 * Adds all particles within radius h to neighbor list
	 * 
	 * @param listOfParticles
	 *            Vector of particles in nearby cubes
	 * @param h
	 *            neighboring radius
	 */
	public void updateNeighborList(Vector<Particle> listOfParticles) {
		mNeighborList.clear();
		for (Particle particle : listOfParticles) {
			float distance = distance(particle);
			if (distance <= mH) {
				mNeighborList.add(particle);
				// if(mNeighborList.size() >= 40)
				// return;
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
	public void initialInCube(Vector<MCCube> allCubes) {
		for (MCCube cube : allCubes) {
			if (cube.containsParticle(this)) {
				mInCube = cube;
				cube.addParticle(this);
				break;
			}
		}
	}

	/**
	 * Determine which cube the particle is in from particle position Loops
	 * through the neighbours of the current cube
	 * 
	 * @return new cube chich the particle is in
	 */
	public void updateInCube(Liquid liquid) {

		// Debug.println(cubeId, Debug.MAX_DEBUG);
		MCCube newCube = liquid.getGrid().getCubeAt(mPosition);

		mInCube.removeParticle(this);
		mInCube = newCube;
		mInCube.addParticle(this);

		// if(!mInCube.containsParticle(this)){
		// Debug.println("Particle position fail", Debug.MAX_DEBUG);
		// Debug.println("Particle pos" + mPosition + " Cube pos: " +
		// mInCube.getPosition(), Debug.MAX_DEBUG);
		// Debug.println(" ", Debug.MAX_DEBUG);
		// }
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
		// Vector3f r_vec = new Vector3f();
		Vector3f r_vec = Vector3f.sub(mPosition, neighbor.getPosition(), null);
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
	 * Calculates pressureforce from object
	 * 
	 * @return
	 */
	private Vector3f pressureForce(Vector3f rvec, float mass, float density) {
		Vector3f pressureForce = new Vector3f();
		float temp = -(mass * mPressure / (2 * density));
		pressureForce = mKernel.GradW_pressure(rvec);
		pressureForce.scale(temp);
		return pressureForce;
	}

	/**
	 * Calculates viscosity force of particle from neighboring particle
	 * 
	 * @return
	 */
	private Vector3f viscosityForce(Vector3f rvec, float mass, float density) {
		Vector3f v_vec = new Vector3f();
		v_vec = (Vector3f) mVelocity.scale(-1);
		Vector3f viscosityForce = v_vec;
		viscosityForce.scale(mEta * mass / density * mKernel.LapW_viscosity(rvec));
		return viscosityForce;
	}

	private void checkBoundaries(Liquid liquid) {
		Boundaries b = liquid.getBoundaries();
		float mass = 100000;
		float density = 1000f;
		Vector3f rvec;
		float k = 0.9f;
		if (b.isSideConstraintsOn())
		{
			if (mPosition.x <= b.xLow + mH) {
				if (mPosition.x <= b.xLow)
					mPosition.x = b.xLow;
				rvec = new Vector3f(mH, 0, 0);
				mForce = Vector3f.add(mForce, handleCollision(rvec, mass, density), null);
				mInCorner = true;
			}
			else if (mPosition.x >= b.xHigh - mH) {
				if (mPosition.x >= b.xHigh)
					mPosition.x = b.xHigh;
				rvec = new Vector3f(-mH, 0, 0);
				mForce = Vector3f.add(mForce, handleCollision(rvec, mass, density), null);

				mInCorner = true;
			}

			if (mPosition.z <= b.zLow + mH) {
				if (mPosition.z <= b.zLow)
					mPosition.z = b.zLow;
				rvec = new Vector3f(0, 0, mH);
				mForce = Vector3f.add(mForce, handleCollision(rvec, mass, density), null);
				mInCorner = true;
			}
			else if (mPosition.z >= b.zHigh - mH) {
				if (mPosition.z >= b.zHigh)
					mPosition.z = b.zHigh;
				rvec = new Vector3f(0, 0, -mH);
				mForce = Vector3f.add(mForce, handleCollision(rvec, mass, density), null);
				mInCorner = true;
			}
		}

		if (mPosition.y < b.yLow + mH) {
			if (mPosition.y <= b.yLow)
				mPosition.y = b.yLow;
			rvec = new Vector3f(0, mH, 0);
			mForce = Vector3f.add(mForce, handleCollision(rvec, mass, density), null);
		}
		else if (mPosition.y > b.yHigh - mH) {
			if (mPosition.y >= b.yHigh)
				mPosition.y = b.yHigh;
			rvec = new Vector3f(0, -mH, 0);
			mForce = Vector3f.add(mForce, handleCollision(rvec, mass, density), null);
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

	public synchronized Vector3f getVelocity() {
		return mVelocity;
	}

	public synchronized Vector3f getPosition() {
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
}
