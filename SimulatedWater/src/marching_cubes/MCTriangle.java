package marching_cubes;

import org.lwjgl.util.vector.Vector3f;

import Rendering.Model;
import Utils.GLUtils;

/**
 * Triangle made up of three vectors
 *
 */
public class MCTriangle{
	public Vector3f pos;
	public Vector3f[] p;
	public Vector3f[] n;
	final byte[] iArray = {0,2,1};
	public Model mModel;
	
	public MCTriangle(){
		p = new Vector3f[3];
		n = new Vector3f[3];
		mModel = new Model();
	}
	
	public MCTriangle(Vector3f p1, Vector3f p2,Vector3f p3,Vector3f n1, Vector3f n2, Vector3f n3){
		p = new Vector3f[3];
		n = new Vector3f[3];
		mModel = new Model();
		
		p[0] = p1;
		p[1] = p2;
		p[2] = p3;
		
		n[0] = n1;
		n[1] = n2;
		n[2] = n3;
	}
	
	//TODO color support
	/**
	 * Draws the triangle
	 * First creates the model with {@link Model.set(...)}
	 * Then uploads the model to OpenGL with {@link Model.draw(...)}
	 * Has to be called from a OPenGL context
	 * @param program The program to be used
	 */
	public synchronized void draw(int program){
		float[] pArray = GLUtils.toArray(p);
		float[] nArray = GLUtils.toArray(n);
		//float[] nArray = GLUtils.toArray(n);
		mModel.set(pArray, nArray, null, null, iArray);
		mModel.draw(program, "in_Position", "in_Normal", null);
	}
	
	/**
	 * Frees the buffers of member model
	 */
	public synchronized void freeModel(){
		mModel.clear();
	}
}