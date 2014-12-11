package Rendering;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Utils.Debug;


public class Renderer implements Runnable{
	
	// Setup variables
	protected String WINDOW_TITLE = "Simulated glass of water";
	protected int mWIDTH;
	protected int mHEIGHT;
	
	//Initiate camera
	protected Camera mCamera;
	
	public void loadShaders(){
	}
	
	public Renderer(int WIDTH,int HEIGHT){
		mWIDTH = WIDTH;
		mHEIGHT = HEIGHT;
	};
	
	protected void setupOpenGL() {
		// Setup an OpenGL context with API version 3.2
		try {
			PixelFormat pixelFormat = new PixelFormat();
			ContextAttribs contextAtrributes = new ContextAttribs(3, 2)
				.withForwardCompatible(true)
				.withProfileCore(true);
			
			Display.setDisplayMode(new DisplayMode(mWIDTH, mHEIGHT));
			Display.setTitle(WINDOW_TITLE);
			Display.create(pixelFormat, contextAtrributes);
			
			glViewport(0, 0, mWIDTH, mHEIGHT);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		// Setup an XNA like background color
		glClearColor(0.4f, 0.6f, 0.9f, 0f);
		
		// Map the internal OpenGL coordinate system to the entire screen
		glViewport(0, 0, mWIDTH, mHEIGHT);	
	}
	
	protected FloatBuffer matrix4Buffer(Matrix4f mat){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		mat.storeTranspose(buffer);
		buffer.flip();
		return buffer;
	}
	
	protected FloatBuffer vector3ArrayBuffer(Vector3f[] vArray){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(vArray.length*3);
		for(Vector3f v:vArray){
			v.store(buffer);
		}
		buffer.flip();
		return buffer;
	}
	
	protected FloatBuffer floatArrayBuffer(float[] fArray){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(fArray.length);
		buffer.put(fArray);
		buffer.flip();
		return buffer;
	}
	
	
	protected void init(){
		setupOpenGL();
		loadShaders();
		Debug.println("INITIATED OPENGL", Debug.MAX_DEBUG);
		//Setup camera
		mCamera = new Camera();
		mCamera.init(new Vector3f(0.25f, 0.25f,-0.5f),new Vector3f(0.25f, 0.25f, 0.25f),new Vector3f(0f, 1f, 0f), 90f, mWIDTH/mHEIGHT, 0.0001f, 1000f);
	}
	
	protected void draw(){
		//To implemented in subclass
	}

	@Override
	public void run() {
		init();
		while(!Display.isCloseRequested()){
			draw();
			// Force a maximum FPS of about 30 ----> dT = 1/30 = 0.033
			Display.sync(30);
			// Let the CPU synchronize with the GPU if GPU is tagging behind
			Display.update();
		}
	}
}
