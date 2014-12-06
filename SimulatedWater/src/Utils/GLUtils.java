package Utils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;


public class GLUtils {
	
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
	 * read and load shaders
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


