package marching_cubes;

import org.lwjgl.util.vector.Vector3f;

public class MCVoxel {
	private Vector3f mPosition;
	private float mValue;
	
	public MCVoxel(Vector3f position, float value){
		mPosition = new Vector3f(position);
		mValue = value;
	}
	
	public MCVoxel(float x, float y, float z, float value){
		mPosition = new Vector3f(x,y,z);
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

	public void addValue(float value) {
		mValue += value;
		
	}
}
