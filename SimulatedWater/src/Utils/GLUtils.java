package Utils;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Class that implements different utilities to support LWJGL opengl implementation
 * @author Martin
 *
 */
public class GLUtils {
	
	public static FloatBuffer matrix4Buffer(Matrix4f mat){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		mat.storeTranspose(buffer);
		buffer.flip();
		return buffer;
	}
	
	public static FloatBuffer vector3ArrayBuffer(Vector3f[] vArray){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(vArray.length*3);
		for(Vector3f v:vArray){
			v.store(buffer);
		}
		buffer.flip();
		return buffer;
	}
	
	public static FloatBuffer floatArrayBuffer(float[] fArray){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(fArray.length);
		buffer.put(fArray);
		buffer.flip();
		return buffer;
	}
	
	/**
	 * Conver array of Vector3f to appended array
	 * @param vectorArray
	 * @return
	 */
	public static float[] toArray(Vector3f[] vectorArray){
		float[] result = new float[vectorArray.length*3];
		for(int i = 0;i<vectorArray.length;i++){
			result[i*3+0] = vectorArray[i].x;
			result[i*3+1] = vectorArray[i].y;
			result[i*3+2] = vectorArray[i].z;
		}
		return result;
	}
	
	/**
	 * Converts ArrayList<FloatByte> to float-array
	 * @param arrayList
	 * @return
	 */
	public static float[] convertToFloatArray(ArrayList<Float> arrayList)
	{
	  float[] array = new float[arrayList.size()];
	  for(int i=0;i<arrayList.size();i++)
	  {
	    array[i] = (float) arrayList.get(i);
	  }
	  return array;
	}
	
	/**
	 * Converts ArrayList<Byte> to byte-array
	 * @param arrayList
	 * @return
	 */
	public static byte[] convertToByteArray(ArrayList<Byte> arrayList)
	{
	  byte[] array = new byte[arrayList.size()];
	  for(int i=0;i<arrayList.size();i++)
	  {
	    array[i] = (byte) arrayList.get(i);
	  }
	  return array;
	}
	
	/**
	 * Converts ArrayList<Short> to short-array
	 * @param arrayList
	 * @return
	 */
	public static short[] convertToShortArray(ArrayList<Short> arrayList)
	{
	  short[] array = new short[arrayList.size()];
	  for(int i=0;i<arrayList.size();i++)
	  {
	    array[i] = (short) arrayList.get(i);
	  }
	  return array;
	}
	
	/**
	 * Read and load shaders from file location
	 * 
	 * Implementation from guide by Oskar Veerhoek
	 * https://github.com/OskarVeerhoek/YouTube-tutorials/tree/master/src/utility
	 * 
	 * TODO geometry shader support
	 * @param vertexShaderLocation
	 * @param fragmentShaderLocation
	 * @return
	 */
	public static int loadShaders(String vertexShaderLocation,String fragmentShaderLocation){
		int shaderProgram = glCreateProgram();
		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		
		StringBuilder vertexShaderSource = new StringBuilder();
	    StringBuilder fragmentShaderSource = new StringBuilder();
	    BufferedReader reader = null;
	    
	    try {
	        reader = new BufferedReader(new FileReader(vertexShaderLocation));
	        String line;
	        while ((line = reader.readLine()) != null) {
	            vertexShaderSource.append(line).append('\n');
	        }
	    } catch (IOException e) {
	        System.err.println("Vertex shader wasn't loaded properly.");
	        e.printStackTrace();
	        Display.destroy();
	        System.exit(1);
	    } finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    BufferedReader reader2 = null;
	    try {
	        reader2 = new BufferedReader(new FileReader(fragmentShaderLocation));
	        String line;
	        while ((line = reader2.readLine()) != null) {
	            fragmentShaderSource.append(line).append('\n');
	        }
	    } catch (IOException e) {
	        System.err.println("Fragment shader wasn't loaded properly.");
	        Display.destroy();
	        System.exit(1);
	    } finally {
	        if (reader2 != null) {
	            try {
	                reader2.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    glShaderSource(vertexShader,vertexShaderSource);
	    glCompileShader(vertexShader);
	    if(glGetShaderi(vertexShader,GL_COMPILE_STATUS)== GL_FALSE){
	    	System.err.println("Vertex shader wasn't able to compile properly");
	    }
	    glShaderSource(fragmentShader,fragmentShaderSource);
	    glCompileShader(fragmentShader);
	    if(glGetShaderi(fragmentShader,GL_COMPILE_STATUS)== GL_FALSE){
	    	System.err.println("Fragment shader wasn't able to compile properly");
	    }
	    glAttachShader(shaderProgram,vertexShader);
	    glAttachShader(shaderProgram,fragmentShader);
	    glLinkProgram(shaderProgram);
	    glValidateProgram(shaderProgram);
	    return shaderProgram;
	}


	
}


