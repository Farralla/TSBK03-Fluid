package Rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.Vector;

import marching_cubes.MCGrid;
import marching_cubes.MCTriangle;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Utils.Debug;
import Utils.GLUtils;
import Utils.MathUtils;
import data_types.CollidableSphere;
import data_types.Liquid;
import data_types.Liquid.Boundaries;
import data_types.Particle;

/**
 * Class that extends Renderer which has basic opengl functionaliy
 * Has functions for drawing the fluid in various modes
 *
 */
public class LiquidRenderer extends Renderer {
	
	//Light
	private Vector3f[] mLightSourcesColorsArr;
	private float[] mSpecularExponent;
	private Vector3f[] mLightSourcesDirectionPositions;
	
	//shader programs
	private int particleProgram, surfaceProgram, modelProgram;
	
	//Models
	private Model mParticleModel, mSphereModel, mGlassModel;
	
	//The fluid
	private Liquid mLiquid;

	public LiquidRenderer(int WIDTH, int HEIGHT) {
		super(WIDTH, HEIGHT);
	}
	
	public LiquidRenderer(int WIDTH, int HEIGHT, Liquid liquid){
		super(WIDTH,HEIGHT);
		mLiquid = liquid;
		mLightSourcesColorsArr = new Vector3f[2];
		mSpecularExponent = new float[2];
		mLightSourcesDirectionPositions = new Vector3f[2];
	}
	
	@Override
	public void loadShaders() {
		particleProgram = GLUtils.loadShaders("src/particleShader.vert", "src/particleShader.frag");
		surfaceProgram = GLUtils.loadShaders("src/surfaceShader.vert", "src/surfaceShader.frag");
		modelProgram  = GLUtils.loadShaders("src/modelShader.vert", "src/modelShader.frag");
	}
	
	public void setupLight(){
		mLightSourcesColorsArr[0] = new Vector3f(0.5f, 0.45f, 0.35f);
		mLightSourcesColorsArr[1] = new Vector3f(1.0f, 0.9f, 0.7f);
		
		mSpecularExponent[0] = 1f;
		mSpecularExponent[1] = 60f;
		
		mLightSourcesDirectionPositions[0] = new Vector3f(0.15f, 1f, 0.6f);
		mLightSourcesDirectionPositions[1] = new Vector3f(0.15f, 1f, 0.6f);
 	}
	
	public void uploadLightToShader(int program){
		glUseProgram(program);
		
		FloatBuffer colorBuffer = vector3ArrayBuffer(mLightSourcesColorsArr);
		glUniform3(glGetUniformLocation(program, "lightSourcesColorArr"), colorBuffer);
		
		FloatBuffer positionBuffer = vector3ArrayBuffer(mLightSourcesDirectionPositions);
		glUniform3(glGetUniformLocation(program, "lightSourcesDirPosArr"), positionBuffer);
		
		FloatBuffer specularBuffer = floatArrayBuffer(mSpecularExponent);
		glUniform1(glGetUniformLocation(program, "specularExponent"), specularBuffer);
	}

	/**
	 * Sets up a 3d cube
	 */
	private void setupCube() {

		// The 4 vertices (corners of a particle)
		final float[] vertices = {
				// Front face
				-0.5f, -0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				-0.5f, 0.5f, 0.5f,

				// Back face
				-0.5f, -0.5f, -0.5f,
				-0.5f, 0.5f, -0.5f,
				0.5f, 0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,

				// Top face
				-0.5f, 0.5f, -0.5f,
				-0.5f, 0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				0.5f, 0.5f, -0.5f,

				// Bottom face
				-0.5f, -0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,
				0.5f, -0.5f, 0.5f,
				-0.5f, -0.5f, 0.5f,

				// Right face
				0.5f, -0.5f, -0.5f,
				0.5f, 0.5f, -0.5f,
				0.5f, 0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,

				// Left face
				-0.5f, -0.5f, -0.5f,
				-0.5f, -0.5f, 0.5f,
				-0.5f, 0.5f, 0.5f,
				-0.5f, 0.5f, -0.5f
		};
		

		final short[] indices = {
				0, 1, 2, 0, 2, 3, // front
				4, 5, 6, 4, 6, 7, // back
				8, 9, 10, 8, 10, 11, // top
				12, 13, 14, 12, 14, 15, // bottom
				16, 17, 18, 16, 18, 19, // right
				20, 21, 22, 20, 22, 23 // left
		};

		mParticleModel = new Model(vertices, null, null, null, indices);
	}
	
	/**
	 * Sets up a 3d cube
	 */
	private void setupGlass() {

		// The 4 vertices (corners of a particle)
		final float[] vertices = {
				// Front face
				-0.5f, -0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				-0.5f, 0.5f, 0.5f,

				// Back face
				-0.5f, -0.5f, -0.5f,
				-0.5f, 0.5f, -0.5f,
				0.5f, 0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,

				// Bottom face
				-0.5f, -0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,
				0.5f, -0.5f, 0.5f,
				-0.5f, -0.5f, 0.5f,

				// Right face
				0.5f, -0.5f, -0.5f,
				0.5f, 0.5f, -0.5f,
				0.5f, 0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,

				// Left face
				-0.5f, -0.5f, -0.5f,
				-0.5f, -0.5f, 0.5f,
				-0.5f, 0.5f, 0.5f,
				-0.5f, 0.5f, -0.5f
		};
		
		final float[] normals = {
				// Front face
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,

				// Back face
				0f, 0f, -1f,
				0f, 0f, -1f,
				0f, 0f, -1f,
				0f, 0f, -1f,

				// Bottom face
				0f, -1f, 0f,
				0f, -1f, 0f,
				0f, -1f, 0f,
				0f, -1f, 0f,

				// Right face
				1f, 0f, 0f,
				1f, 0f, 0f,
				1f, 0f, 0f,
				1f, 0f, 0f,

				// Left face
				-1f, 0f, 0f,
				-1f, 0f, 0f,
				-1f, 0f, 0f,
				-1f, 0f, 0f,
		};

		final short[] indices = {
				0, 1, 2, 0, 2, 3, // front
				4, 5, 6, 4, 6, 7, // back
				8, 9, 10, 8, 10, 11,// bottom
				12, 13, 14, 12, 14, 15, // right
				16, 17, 18, 16, 18, 19, // left
		};

		mGlassModel = new Model(vertices, normals, null, null, indices);
	}

	/**
	 * Sets up a square
	 */
	private void setupSquare() {

		// The 4 vertices (corners of a particle)
		final float[] vertices = {
				// Front face
				-0.5f, -0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				-0.5f, 0.5f, 0.5f,
		};

		final short[] indices = {
				0, 1, 2,
				2, 3, 0
		};

		mParticleModel = new Model(vertices, null, null, null, indices);
	}

	/**
	 * Draws liquid depending on mode
	 * @param liquid the liquid to draw
	 */
	public void drawLiquid() {
		if (mLiquid.drawMode() == Liquid.DRAW_PARTICLES) {
			drawParticles();
		}
		else if (mLiquid.drawMode() == Liquid.DRAW_SURFACE) {
			drawLiquid(mLiquid.getGrid());
		}
		else if(mLiquid.drawMode() == Liquid.DRAW_TRIANGLES){
			drawTriangles(mLiquid.getGrid());
		}
	}

	/**
	 * Draws the particles in shape of cubes
	 */
	private void drawParticles() {
		glUseProgram(particleProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram, "viewMatrix"), true, viewMatrixBuffer);
		for (Particle particle : mLiquid.getParticleList()) {
			// Upload modelMatrix to shader
			Matrix4f modelMatrix = MathUtils.transMatrix(particle.getPosition());
			modelMatrix.scale(new Vector3f(0.006f, 0.006f, 0.006f));
			FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
			modelMatrixBuffer.clear();
			glUniformMatrix4(glGetUniformLocation(particleProgram, "modelMatrix"), true, modelMatrixBuffer);

			mParticleModel.draw(particleProgram, "in_Position", null, null);
		}
		// glDisableVertexAttribArray(0);
		// glDisableVertexAttribArray(1);
		// glDisableVertexAttribArray(2);
		// glBindVertexArray(0);
		glUseProgram(0);
	}

	/**
	 * Draws the triangles that makes up the surface of the fluid
	 * @param grid
	 */
	private void drawTriangles(MCGrid grid) {
		glUseProgram(surfaceProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(surfaceProgram, "viewMatrix"), true, viewMatrixBuffer);
		viewMatrixBuffer.clear();

		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0, 0, 0));
		FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(surfaceProgram, "modelMatrix"), true, modelMatrixBuffer);
		modelMatrixBuffer.clear();
		
		Vector<MCTriangle> triangles = (Vector<MCTriangle>) (grid.getTriangles().clone());
		//Debug.println("Triangles " + triangles.size(), Debug.MAX_DEBUG);
		for (final MCTriangle triangle : triangles) {
			triangle.draw(surfaceProgram);
			triangle.freeModel();
		}
		triangles.clear();
		
		glUseProgram(0);
	}
	
	/**
	 * @param grid
	 */
	private void drawLiquid(MCGrid grid){
		glUseProgram(surfaceProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(surfaceProgram, "viewMatrix"), true, viewMatrixBuffer);
		viewMatrixBuffer.clear();

		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0, 0, 0));
		FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(surfaceProgram, "modelMatrix"), true, modelMatrixBuffer);
		modelMatrixBuffer.clear();
		
		if(grid.getVertexPositions() != null){
			float[] v = grid.getVertexPositions();
			short[] i = grid.getIndices();
			float[] n = grid.getNormals();
			Model model = new Model(v,n,null,null,i);
			model.draw(surfaceProgram, "in_Position", "in_Normal", null);
			model.clear();
		}
		glUseProgram(0);
	}
	
	private void drawSpheres() {
		glUseProgram(modelProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(modelProgram, "viewMatrix"), true, viewMatrixBuffer);
		for (CollidableSphere c : mLiquid.getCollidables()) {
			// Upload modelMatrix to shader
			Matrix4f modelMatrix = MathUtils.transMatrix(c.getPosition());
			modelMatrix.scale(new Vector3f(c.getRadius(), c.getRadius(), c.getRadius()));
			FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
			modelMatrixBuffer.clear();
			glUniformMatrix4(glGetUniformLocation(modelProgram, "modelMatrix"), true, modelMatrixBuffer);

			mSphereModel.draw(modelProgram, "in_Position", "in_Normal", null);
		}
		glUseProgram(0);
	}
	
	private void drawGlass(){
		glUseProgram(modelProgram);
		
		Boundaries b  = mLiquid.getBoundaries();
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
		//glEnable(GL_CULL_FACE);
		//glUniform4f(glGetUniformLocation(modelProgram, "color"), 0.2f, 0.2f, 0.2f,0.2f);
		
		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(modelProgram, "viewMatrix"), true, viewMatrixBuffer);
		// Upload modelMatrix to shader
		Matrix4f modelMatrix = MathUtils.transMatrix(b.getPosition());
		modelMatrix.scale(new Vector3f(b.getSize()+0.04f,b.getSize()+0.04f,b.getSize()+0.04f));
		FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
		modelMatrixBuffer.clear();
		
		glUniformMatrix4(glGetUniformLocation(modelProgram, "modelMatrix"), true, modelMatrixBuffer);

		mGlassModel.draw(modelProgram, "in_Position", "in_Normal", null);
		
		glDisable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glUseProgram(0);
	}
	
	@Override
	protected void draw(){
		// Update camera
		mCamera.update();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		checkKeys();
		drawLiquid();
		drawSpheres();
		drawGlass();
	}

	public void checkKeys(){
		if(Keyboard.isKeyDown(Keyboard.KEY_1)){
			mLiquid.setDrawMode(Liquid.DRAW_PARTICLES);
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_2)){
			mLiquid.setDrawMode(Liquid.DRAW_SURFACE);
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_3)){
			mLiquid.setDrawMode(Liquid.DRAW_TRIANGLES);
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)){
			if(!mLiquid.isStarted()){
				mLiquid.setIsStarted(true);
				synchronized(mLiquid){
					mLiquid.notify();
				}

			}else{
				mLiquid.setIsStarted(false);
			}

		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
			if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
				mLiquid.getGrid().changeIsoLevel("decrease");
				Debug.println("Isolevel: " + mLiquid.getGrid().getIsoLevel(),Debug.MAX_DEBUG);
			}
			else if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
				mLiquid.getGrid().changeIsoLevel("increase");
				Debug.println("Isolevel: " + mLiquid.getGrid().getIsoLevel(),Debug.MAX_DEBUG);
			}
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
			Vector3f vec = Liquid.gravity();
			Matrix4f rotMat = new Matrix4f();
			rotMat.setIdentity();
			rotMat.rotate(0.05f, new Vector3f(0,0,1));
			vec = MathUtils.multMat4Vec3(rotMat, vec);
			Liquid.setGravity(vec);
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
			Vector3f vec = Liquid.gravity();
			Matrix4f rotMat = new Matrix4f();
			rotMat.setIdentity();
			rotMat.rotate(0.05f, new Vector3f(0,0,-1));
			vec = MathUtils.multMat4Vec3(rotMat, vec);
			Liquid.setGravity(vec);
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_G)){
			Liquid.setGravity((Vector3f) Liquid.gravity().scale(-1f));
		}
	}
	
	private void setupSphere(){
		SphereCreator sphere = new SphereCreator(1,20,20);
		mSphereModel = new Model(sphere.getVertices(),sphere.getNormals(),null,null,sphere.getIndices());
	}

	
	@Override
	protected void init(){
		super.init();
		glEnable(GL_DEPTH_TEST);
		//Setup cube for particles
		setupCube();
		setupSphere();
		setupGlass();
		Debug.println("CUBE SETUP", Debug.MAX_DEBUG);
		
		setupLight();
		Debug.println("LIGHT SETUP", Debug.MAX_DEBUG);
		
		//Upload projectionMatrix to shaders
		glUseProgram(particleProgram);
		FloatBuffer projectionMatrixBuffer = matrix4Buffer(mCamera.getProjectionMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		
		glUseProgram(surfaceProgram);
		glUniformMatrix4(glGetUniformLocation(surfaceProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		uploadLightToShader(surfaceProgram);
		
		glUseProgram(modelProgram);
		glUniformMatrix4(glGetUniformLocation(modelProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		uploadLightToShader(modelProgram);
	}

}
