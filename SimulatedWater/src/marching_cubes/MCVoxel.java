package marching_cubes;

import org.lwjgl.util.vector.Vector3f;

public class MCVoxel {
	private Vector3f mPosition;
	private Vector3f mNormal;
	private float mValue;
	
	public MCVoxel(Vector3f position, Vector3f normal, float value){
		mPosition = new Vector3f(position);
		mNormal = new Vector3f(normal);
		mValue = value;
	}
	
	public MCVoxel(float x, float y, float z, float value){
		mPosition = new Vector3f(x,y,z);
		mNormal = new Vector3f();
		mValue = value;
	}
	
	public synchronized Vector3f getPosition(){
		return mPosition;
	}
	
	public synchronized float getValue(){
		return mValue;
	}
	
	public synchronized void resetValue(){
		mValue = 0;
	}

	public synchronized void addValue(float value) {
		mValue += value;
		
	}
	
	public Vector3f getNormal(){
		return mNormal;
	}
	
	public void setNormal(Vector3f n){
		n.normalise();
		mNormal = n;
	}
}
