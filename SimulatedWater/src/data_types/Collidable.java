package data_types;

import org.lwjgl.util.vector.Vector3f;

public abstract class Collidable {
	protected float mRadius;
	protected Vector3f mPosition;
	protected float mMass;
	protected float mDensity;
	protected float mPressure;
	protected Vector3f mForce;
	protected Vector3f mVelocity;
	
	public Collidable(Vector3f position, float radius){
		mPosition = position;
		mForce = new Vector3f();
		mVelocity = new Vector3f();
		mRadius = radius;
		mDensity = 6000000f;
		mMass = 0.001f;
		mPressure = 100000f;
	}
	
	public Collidable(Vector3f position, float radius, float mass, float density) {
		mPosition = position;
		mForce = new Vector3f(0,0,0);
		mVelocity = new Vector3f(0,0,0);
		mRadius = radius;
		mMass = mass;
		mDensity = density;
	}
	
	public void addForce(Vector3f force) {
		mForce = Vector3f.add(mForce, force, null);
	}
	
	public void resetForce(){
		mForce.set(0, 0, 0);
	}

	abstract void update();
	
	public Vector3f getPosition(){
		return mPosition;
	}
	
	public float getRadius(){
		return mRadius;
	}
	
	public float getMass(){
		return mMass;
	}
	
	public float getDensity(){
		return mDensity;
	}
	
	public float getPressure(){
		return mPressure;
	}

}
