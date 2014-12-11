package Rendering;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;

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
import data_types.Liquid;
import data_types.Particle;

public class LiquidRenderer extends Renderer {

	private int particleProgram;
	private Model mParticleModel;
	private Liquid mLiquid;

	public LiquidRenderer(int WIDTH, int HEIGHT) {
		super(WIDTH, HEIGHT);
	}
	
	public LiquidRenderer(int WIDTH, int HEIGHT, Liquid liquid){
		super(WIDTH,HEIGHT);
		mLiquid = liquid;
	}
	
	@Override
	public void loadShaders() {
		particleProgram = GLUtils.loadShaders("src/particleShader.vert", "src/particleShader.frag");
	}

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

		final byte[] indices = {
				0, 1, 2, 0, 2, 3, // front
				4, 5, 6, 4, 6, 7, // back
				8, 9, 10, 8, 10, 11, // top
				12, 13, 14, 12, 14, 15, // bottom
				16, 17, 18, 16, 18, 19, // right
				20, 21, 22, 20, 22, 23 // left
		};

		mParticleModel = new Model(vertices, null, null, null, indices);
	}

	private void setupSquare() {

		// The 4 vertices (corners of a particle)
		final float[] vertices = {
				// Front face
				-0.5f, -0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				-0.5f, 0.5f, 0.5f,
		};

		final byte[] indices = {
				0, 1, 2,
				2, 3, 0
		};

		mParticleModel = new Model(vertices, null, null, null, indices);
	}

	public void drawLiquid(Liquid liquid) {
		if (liquid.drawMode() == Liquid.DRAW_PARTICLES) {
			drawParticles(liquid);
		}
		else if (liquid.drawMode() == Liquid.DRAW_SURFACE) {
			drawTriangles(liquid.getGrid());
		}
	}

	private void drawParticles(Liquid liquid) {

		// Update camera
		mCamera.update();

		glClear(GL_COLOR_BUFFER_BIT);
		glUseProgram(particleProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram, "viewMatrix"), true, viewMatrixBuffer);
		for (Particle particle : liquid.getParticleList()) {
			// Upload modelMatrix to shader
			Matrix4f modelMatrix = MathUtils.transMatrix(particle.getPosition());
			modelMatrix.scale(new Vector3f(0.01f, 0.01f, 0.01f));
			FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
			glUniformMatrix4(glGetUniformLocation(particleProgram, "modelMatrix"), true, modelMatrixBuffer);

			mParticleModel.draw(particleProgram, "in_Position", null, null);
		}
		// glDisableVertexAttribArray(0);
		// glDisableVertexAttribArray(1);
		// glDisableVertexAttribArray(2);
		// glBindVertexArray(0);
		glUseProgram(0);
	}

	private void drawTriangles(MCGrid grid) {
		// Update camera
		mCamera.update();

		glClear(GL_COLOR_BUFFER_BIT);
		glUseProgram(particleProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram, "viewMatrix"), true, viewMatrixBuffer);

		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0, 0, 0));
		FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(particleProgram, "modelMatrix"), true, modelMatrixBuffer);
		
		Vector<MCTriangle> triangles = (Vector<MCTriangle>) grid.getTriangles().clone();
		for (MCTriangle triangle : triangles) {
			triangle.draw(particleProgram);
		}
	}

	public void drawMCTriangle(MCTriangle triangle) {
		// Update camera
		mCamera.update();

		glClear(GL_COLOR_BUFFER_BIT);
		glUseProgram(particleProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram, "viewMatrix"), true, viewMatrixBuffer);

		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0, 0, 0));
		FloatBuffer modelMatrixBuffer = matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(particleProgram, "modelMatrix"), true, modelMatrixBuffer);
		triangle.draw(particleProgram);
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
	}
	
	@Override
	protected void init(){
		super.init();
		
		setupCube();
		Debug.println("CUBE SETUP", Debug.MAX_DEBUG);
		//Upload projectionMatrix to shader
		glUseProgram(particleProgram);
		FloatBuffer projectionMatrixBuffer = matrix4Buffer(mCamera.getProjectionMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram,"projectionMatrix"), true, projectionMatrixBuffer);
	}

}
