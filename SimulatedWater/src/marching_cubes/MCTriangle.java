package marching_cubes;

import org.lwjgl.util.vector.Vector3f;

import Rendering.Model;
import Utils.GLUtils;
import Utils.Timer;

/**
 * Triangle made up of three vectors
 * @author Martin
 *
 */
public class MCTriangle{
	public Vector3f pos;
	public Vector3f[] p;
	public Vector3f[] n;
	final byte[] i = {0,1,2};
	public Model mModel;
	
	public MCTriangle(){
		p = new Vector3f[3];
		n = new Vector3f[3];
		mModel = new Model();
	}
	
	public MCTriangle(Vector3f p1, Vector3f p2,Vector3f p3){
		p = new Vector3f[3];
		n = new Vector3f[3];
		mModel = new Model();
		
		p[0] = p1;
		p[1] = p2;
		p[2] = p3;
		
	}
	
	//TODO normals, color support
	/**
	 * Draws the triangle
	 * First creates the model with {@link Model.set(...)}
	 * Then uploads the model to OpenGL with {@link Model.draw(...)}
	 * @param program The program to be used
	 */
	public synchronized void draw(int program){
		float[] pArray = GLUtils.toArray(p);
		//float[] nArray = GLUtils.toArray(n);
		mModel.set(pArray, null, null, null, i);
		mModel.draw(program, "in_Position", null, null);

		
	}
}