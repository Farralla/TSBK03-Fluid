package Rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.Vector;

import marching_cubes.MCGrid;
import marching_cubes.MCTriangle;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.badlogic.gdx.utils.BufferUtils;

import Utils.Debug;
import Utils.GLUtils;
import Utils.MathUtils;
import data_types.Liquid;
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
	private int particleProgram, surfaceProgram;
	
	//Models
	private Model mParticleModel;
	
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
	}
	
	public void setupLight(){
		mLightSourcesColorsArr[0] = new Vector3f(0.5f, 0.45f, 0.35f);
		mLightSourcesColorsArr[1] = new Vector3f(1.0f, 0.9f, 0.7f);
		
		mSpecularExponent[0] = 1f;
		mSpecularExponent[1] = 5f;
		
		mLightSourcesDirectionPositions[0] = new Vector3f(0.3f, 0.2f, 0.7f);
		mLightSourcesDirectionPositions[1] = new Vector3f(0.3f, 0.2f, 0.7f);
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
	public void drawLiquid(Liquid liquid) {
		if (liquid.drawMode() == Liquid.DRAW_PARTICLES) {
			drawParticles(liquid);
		}
		else if (liquid.drawMode() == Liquid.DRAW_SURFACE) {
			drawLiquid(liquid.getGrid());
		}
		else if(liquid.drawMode() == Liquid.DRAW_TRIANGLES){
			drawTriangles(liquid.getGrid());
		}
	}

	/**
	 * Draws the particles in shape of cubes
	 * @param liquid
	 */
	private void drawParticles(Liquid liquid) {

		// Update camera
		mCamera.update();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glUseProgram(particleProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram, "viewMatrix"), true, viewMatrixBuffer);
		for (Particle particle : liquid.getParticleList()) {
			// Upload modelMatrix to shader
			Matrix4f modelMatrix = MathUtils.transMatrix(particle.getPosition());
			modelMatrix.scale(new Vector3f(0.01f, 0.01f, 0.01f));
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
		// Update camera
		mCamera.update();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
	 * TODO Not working correcly, indices need fixing
	 * Still as laggy as with triangle approach
	 * @param grid
	 */
	private void drawLiquid(MCGrid grid){
		mCamera.update();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
	
	public int getParticleProgram(){
		return particleProgram;
	}
	
	@Override
	protected void draw(){
		checkKeys();
		drawLiquid(mLiquid);
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
		else if(Keyboard.isKeyDown(Keyboard.KEY_O)){
			mLiquid.getBoundaries().setSideConstraintsOn(false);
		}
	}
	
	@Override
	protected void init(){
		super.init();
		glEnable(GL_DEPTH_TEST);
		//Setup cube for particles
		setupCube();
		Debug.println("CUBE SETUP", Debug.MAX_DEBUG);
		
		setupLight();
		Debug.println("LIGHT SETUP", Debug.MAX_DEBUG);
		
		//Upload projectionMatrix to shader
		glUseProgram(particleProgram);
		FloatBuffer projectionMatrixBuffer = matrix4Buffer(mCamera.getProjectionMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		
		glUseProgram(surfaceProgram);
		glUniformMatrix4(glGetUniformLocation(surfaceProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		uploadLightToShader(surfaceProgram);
	}

}
