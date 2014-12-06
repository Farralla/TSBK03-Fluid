package Rendering;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.FloatBuffer;

import marching_cubes.MCGrid;
import marching_cubes.MCTriangle;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Utils.GLUtils;
import Utils.MathUtils;
import Utils.Model;
import data_types.Liquid;
import data_types.Particle;


public class Renderer{
	
	// Setup variables
	private final String WINDOW_TITLE = "Simulated glass of water";
	private int mWIDTH;
	private int mHEIGHT;
	
	//Initiate camera
	Camera mCamera;
	
	//Shaders
	private int basicProgram;
	private int particleProgram;
	
	private Model mParticleModel;
	
	public void loadShaders(){
		particleProgram = GLUtils.loadShaders("src/particleShader.vert", "src/particleShader.frag");
	}
	
	public Renderer(int WIDTH,int HEIGHT){
		mWIDTH = WIDTH;
		mHEIGHT = HEIGHT;
	};
	
	public void setupOpenGL() {
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
	
	public void setupCube(){
		
		// The 4 vertices (corners of a particle)
		final float[] vertices = {
				// Front face
				-0.5f, -0.5f,  0.5f,
				0.5f, -0.5f,  0.5f,
				0.5f,  0.5f,  0.5f,
				-0.5f,  0.5f,  0.5f,
		                              
				// Back face
				-0.5f, -0.5f, -0.5f,
				-0.5f,  0.5f, -0.5f,
				0.5f,  0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,
		                              
				// Top face
				-0.5f,  0.5f, -0.5f,
				-0.5f,  0.5f,  0.5f,
				0.5f,  0.5f,  0.5f,
				0.5f,  0.5f, -0.5f,
		                              
				// Bottom face
				-0.5f, -0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,
				0.5f, -0.5f,  0.5f,
				-0.5f, -0.5f,  0.5f,
		                              
				// Right face
				0.5f, -0.5f, -0.5f,
				0.5f,  0.5f, -0.5f,
				0.5f,  0.5f,  0.5f,
				0.5f, -0.5f,  0.5f,
		                              
				// Left face
				-0.5f, -0.5f, -0.5f,
				-0.5f, -0.5f,  0.5f,
				-0.5f,  0.5f,  0.5f,
				-0.5f,  0.5f, -0.5f
		};
		
		final byte[] indices = {
				0,  1,  2,      0,  2,  3,    // front
				4,  5,  6,      4,  6,  7,    // back
				8,  9,  10,     8,  10, 11,   // top
				12, 13, 14,     12, 14, 15,   // bottom
				16, 17, 18,     16, 18, 19,   // right
				20, 21, 22,     20, 22, 23    // left
		};
		
		mParticleModel = new Model(vertices, null, null, null, indices);
	}
	
	public void setupSquare(){
		
		// The 4 vertices (corners of a particle)
		final float[] vertices = {
				// Front face
				-0.5f, -0.5f,  0.5f,
				0.5f, -0.5f,  0.5f,
				0.5f,  0.5f,  0.5f,
				-0.5f,  0.5f,  0.5f,
		};
		
		final byte[] indices = {
				0, 1, 2,
				2, 3, 0
		};
		
		mParticleModel = new Model(vertices, null, null, null, indices);
	}
	
	public FloatBuffer matrix4Buffer(Matrix4f mat){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		mat.storeTranspose(buffer); //Shopuld this be non-transposed?
		buffer.flip();
		return buffer;
	}
	
	public void drawLiquid(Liquid liquid){
		if(liquid.drawMode() == Liquid.DRAW_PARTICLES){
			drawParticles(liquid);
		}
		else if(liquid.drawMode() == Liquid.DRAW_SURFACE){
			drawTriangles(liquid.getGrid());
		}
	}
	
	public void drawParticles(Liquid liquid){
		
		//Update camera
		mCamera.update();
		
		glClear(GL_COLOR_BUFFER_BIT);
		glUseProgram(particleProgram);
		
		//Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram,"viewMatrix"), true, viewMatrixBuffer);
		for(Particle particle:liquid.getParticleList()){
			//Upload modelMatrix to shader
			Matrix4f modelMatrix = MathUtils.transMatrix(particle.getPosition());
			modelMatrix.scale(new Vector3f(0.001f,0.001f,0.001f));
			FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
			glUniformMatrix4(glGetUniformLocation(particleProgram,"modelMatrix"), true, modelMatrixBuffer);
			
			mParticleModel.draw(particleProgram,"in_Position",null,null);	
		}
		//glDisableVertexAttribArray(0);
		//glDisableVertexAttribArray(1);
		//glDisableVertexAttribArray(2);
		//glBindVertexArray(0);
		glUseProgram(0);	
	}
	
	public void drawTriangles(MCGrid grid){
		//Update camera
		mCamera.update();
		
		glClear(GL_COLOR_BUFFER_BIT);
		glUseProgram(particleProgram);
		
		//Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram,"viewMatrix"), true, viewMatrixBuffer);
		
		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0,0,0));
		FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(particleProgram,"modelMatrix"), true, modelMatrixBuffer);
		
		for(MCTriangle triangle:grid.getTriangles()){
			triangle.draw(particleProgram);
		}
	}
	
	public void drawMCTriangle(MCTriangle triangle){
		//Update camera
		mCamera.update();
		
		glClear(GL_COLOR_BUFFER_BIT);
		glUseProgram(particleProgram);
		
		//Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram,"viewMatrix"), true, viewMatrixBuffer);
		
		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0,0,0));
		FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(particleProgram,"modelMatrix"), true, modelMatrixBuffer);
		triangle.draw(particleProgram);
	}
	
	public void init(){
		setupOpenGL();
		loadShaders();
		setupCube();
		
		//Setup camera
		mCamera = new Camera();
		mCamera.init(new Vector3f(0.025f, 0.025f,-0.05f),new Vector3f(0.025f, 0.025f, 0.025f),new Vector3f(0f, 1f, 0f), 90f, mWIDTH/mHEIGHT, 0.0001f, 1000f);
		
		//Upload projectionMatrix to shader
		glUseProgram(particleProgram);
		FloatBuffer projectionMatrixBuffer = matrix4Buffer(mCamera.getProjectionMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram,"projectionMatrix"), true, projectionMatrixBuffer);
	}
	
	public int getParticleProgram(){
		return particleProgram;
	}
	

}
