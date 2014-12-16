package data_types;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

abstract class Boundaries {
	protected ArrayList<Vector3f> mNormals;
	protected Vector3f mPosition;
	protected float mDensity;
	
	public Boundaries(Vector3f position){
		mPosition = position;
		mNormals = new ArrayList<Vector3f>();
		mDensity = 1000;
	}
	
	public abstract void Rotate(Vector3f axis, float degrees);
	
	public abstract void checkCollisions(Particle particle);
	public abstract void checkCollisions(CollidableSphere c);
}
