package rendering;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.FloatBuffer;
import java.util.Vector;

import marching_cubes.MCGrid;
import marching_cubes.MCTriangle;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import visuals.Skybox;
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
	private int particleProgram, surfaceProgram, modelProgram, reflectionProgram;
	
	//Models
	private Model mParticleModel, mSphereModel, mGlassModel, mFloorModel;
	
	//The fluid
	private Liquid mLiquid;
	
	//Skybox
	private Skybox mSkybox;

	public LiquidRenderer(int WIDTH, int HEIGHT) {
		super(WIDTH, HEIGHT);
	}
	
	/**
	 * Cteates a renderer and initates some vectors
	 * @param WIDTH
	 * @param HEIGHT
	 * @param liquid
	 */
	public LiquidRenderer(int WIDTH, int HEIGHT, Liquid liquid){
		super(WIDTH,HEIGHT);
		mLiquid = liquid;
		mLightSourcesColorsArr = new Vector3f[2];
		mSpecularExponent = new float[2];
		mLightSourcesDirectionPositions = new Vector3f[2];
	}
	
	@Override
	public void loadShaders() {
		particleProgram = GLUtils.loadShaders("src/shaders/particleShader.vert", "src/shaders/particleShader.frag");
		surfaceProgram = GLUtils.loadShaders("src/shaders/surfaceShader.vert", "src/shaders/surfaceShader.frag");
		modelProgram  = GLUtils.loadShaders("src/shaders/modelShader.vert", "src/shaders/modelShader.frag");
		reflectionProgram  = GLUtils.loadShaders("src/shaders/reflection.vert", "src/shaders/reflection.frag");
	}
	
	/**
	 * Sets up the light vectors
	 */
	public void setupLight(){
		mLightSourcesColorsArr[0] = new Vector3f(0.5f, 0.45f, 0.35f);
		mLightSourcesColorsArr[1] = new Vector3f(1.0f, 0.9f, 0.7f);
		
		mSpecularExponent[0] = 1f;
		mSpecularExponent[1] = 60f;
		
		mLightSourcesDirectionPositions[0] = new Vector3f(0.15f, 1f, 0.6f);
		mLightSourcesDirectionPositions[1] = new Vector3f(0.15f, 1f, 0.6f);
 	}
	
	/**
	 * Uploads light vectors to shader
	 * @param program shader to upload light to
	 */
	public void uploadLightToShader(int program){
		glUseProgram(program);
		
		FloatBuffer colorBuffer = GLUtils.vector3ArrayBuffer(mLightSourcesColorsArr);
		glUniform3(glGetUniformLocation(program, "lightSourcesColorArr"), colorBuffer);
		
		FloatBuffer positionBuffer = GLUtils.vector3ArrayBuffer(mLightSourcesDirectionPositions);
		glUniform3(glGetUniformLocation(program, "lightSourcesDirPosArr"), positionBuffer);
		
		FloatBuffer specularBuffer = GLUtils.floatArrayBuffer(mSpecularExponent);
		glUniform1(glGetUniformLocation(program, "specularExponent"), specularBuffer);
	}

	/**
	 * Uploads the skybox texture to the reflection program
	 */
	void initReflectionObj()
	 {
		glUseProgram(reflectionProgram);
		glUniform1i(glGetUniformLocation(reflectionProgram, "cubeMap"), 16); //Texture unit 16: skybox
	 }

	/**
	 * Draws liquid depending on mode
	 */
	public void drawLiquid() {
		switch(mLiquid.drawMode()){
		case Liquid.DRAW_PARTICLES:
			drawParticles();
			break;
		case Liquid.DRAW_SURFACE:
			drawLiquid(mLiquid.getGrid());
			break;
		case Liquid.DRAW_SURFACE_REFLECTED:
			drawLiquidReflected(mLiquid.getGrid());
			break;
		case Liquid.DRAW_TRIANGLES:
			drawTriangles(mLiquid.getGrid());
			break;
		default:
			drawLiquidReflected(mLiquid.getGrid());
		}
	}

	/**
	 * Draws the particles in shape of cubes
	 */
	private void drawParticles() {
		glUseProgram(particleProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = GLUtils.matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram, "viewMatrix"), true, viewMatrixBuffer);
		for (Particle particle : mLiquid.getParticleList()) {
			// Upload modelMatrix to shader
			Matrix4f modelMatrix = MathUtils.transMatrix(particle.getPosition());
			modelMatrix.scale(new Vector3f(0.006f, 0.006f, 0.006f));
			FloatBuffer modelMatrixBuffer = GLUtils.matrix4Buffer(modelMatrix);
			modelMatrixBuffer.clear();
			glUniformMatrix4(glGetUniformLocation(particleProgram, "modelMatrix"), true, modelMatrixBuffer);

			mParticleModel.draw(particleProgram, "in_Position", null, null);
		}
		glUseProgram(0);
	}

	/**
	 * Draws the triangles that makes up the surface of the fluid
	 * @param grid
	 */
	private void drawTriangles(MCGrid grid) {
		glUseProgram(surfaceProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = GLUtils.matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(surfaceProgram, "viewMatrix"), true, viewMatrixBuffer);
		viewMatrixBuffer.clear();

		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0, 0, 0));
		FloatBuffer modelMatrixBuffer = GLUtils.matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(surfaceProgram, "modelMatrix"), true, modelMatrixBuffer);
		modelMatrixBuffer.clear();
		
		@SuppressWarnings("unchecked")
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
	 * Draws the liquid without environment mapping
	 * @param grid
	 */
	private void drawLiquid(MCGrid grid){
		glUseProgram(surfaceProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = GLUtils.matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(surfaceProgram, "viewMatrix"), true, viewMatrixBuffer);
		viewMatrixBuffer.clear();

		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0, 0, 0));
		FloatBuffer modelMatrixBuffer = GLUtils.matrix4Buffer(modelMatrix);
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
	
	/**
	 * Draws the spheres
	 */
	private void drawSpheres() {
		glUseProgram(modelProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = GLUtils.matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(modelProgram, "viewMatrix"), true, viewMatrixBuffer);
		for (CollidableSphere c : mLiquid.getCollidables()) {
			// Upload modelMatrix to shader
			Matrix4f modelMatrix = MathUtils.transMatrix(c.getPosition());
			modelMatrix.scale(new Vector3f(c.getRadius(), c.getRadius(), c.getRadius()));
			FloatBuffer modelMatrixBuffer = GLUtils.matrix4Buffer(modelMatrix);
			modelMatrixBuffer.clear();
			glUniformMatrix4(glGetUniformLocation(modelProgram, "modelMatrix"), true, modelMatrixBuffer);

			mSphereModel.draw(modelProgram, "in_Position", "in_Normal", null);
		}
		glUseProgram(0);
	}
	
	/**
	 * Draws the glass cube
	 */
	private void drawGlass(){
		glUseProgram(modelProgram);
		
		Boundaries b  = mLiquid.getBoundaries();
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
		//glEnable(GL_CULL_FACE);
		//glUniform4f(glGetUniformLocation(modelProgram, "color"), 0.2f, 0.2f, 0.2f,0.2f);
		
		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = GLUtils.matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(modelProgram, "viewMatrix"), true, viewMatrixBuffer);
		// Upload modelMatrix to shader
		Matrix4f modelMatrix = MathUtils.transMatrix(b.getPosition());
		modelMatrix.scale(new Vector3f(b.getSize()+0.05f,b.getSize()+0.05f,b.getSize()+0.05f));
		FloatBuffer modelMatrixBuffer = GLUtils.matrix4Buffer(modelMatrix);
		modelMatrixBuffer.clear();
		
		glUniformMatrix4(glGetUniformLocation(modelProgram, "modelMatrix"), true, modelMatrixBuffer);

		mGlassModel.draw(modelProgram, "in_Position", "in_Normal", null);
		
		glDisable(GL_BLEND);
		//glDisable(GL_CULL_FACE);
		glUseProgram(0);
	}
	
	/**
	 * Draws liquid with environment mapping
	 * @param grid
	 */
	private void drawLiquidReflected(MCGrid grid){
		glUseProgram(reflectionProgram);
		glEnable(GL_BLEND);
		glEnable(GL_CULL_FACE);
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
		glCullFace(GL_BACK);
		glCullFace(GL_FRONT);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = GLUtils.matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(reflectionProgram, "viewMatrix"), true, viewMatrixBuffer);
		viewMatrixBuffer.clear();

		Matrix4f modelMatrix = MathUtils.transMatrix(new Vector3f(0, 0, 0));
		FloatBuffer modelMatrixBuffer = GLUtils.matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(reflectionProgram, "modelMatrix"), true, modelMatrixBuffer);
		modelMatrixBuffer.clear();
		
		if(grid.getVertexPositions() != null){
			float[] v = grid.getVertexPositions();
			short[] i = grid.getIndices();
			float[] n = grid.getNormals();
			Model model = new Model(v,n,null,null,i);
			model.draw(reflectionProgram, "in_Position", "in_Normal", null);
			model.clear();
		}
		glDisable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glUseProgram(0);
	}
	
	private void drawFloor(){
		glUseProgram(modelProgram);

		// Upload viewMAtrix to shader
		FloatBuffer viewMatrixBuffer = GLUtils.matrix4Buffer(mCamera.getViewMatrix());
		glUniformMatrix4(glGetUniformLocation(modelProgram, "viewMatrix"), true, viewMatrixBuffer);
		
		// Upload modelMatrix to shader
		Matrix4f modelMatrix = MathUtils.transMatrix(0.15f,-0.04f,0.15f);
		modelMatrix.scale(new Vector3f(1, 1, 1));
		FloatBuffer modelMatrixBuffer = GLUtils.matrix4Buffer(modelMatrix);
		glUniformMatrix4(glGetUniformLocation(modelProgram, "modelMatrix"), true, modelMatrixBuffer);

		mFloorModel.draw(modelProgram, "in_Position", "in_Normal", null);
		glUseProgram(0);
	}
	
	@Override
	protected void draw(){
		// Update camera
		mCamera.update();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		checkKeys();
		mSkybox.draw(mCamera.getViewMatrix());
		drawSpheres();
		drawFloor();
		drawLiquid();
		drawGlass();
	}
	
	/**
	 * Check if any action keys has been pressed
	 */
	public void checkKeys(){
		
		while (Keyboard.next()) {
		    if (Keyboard.getEventKeyState()) {
		        switch (Keyboard.getEventKey()) {
		        case Keyboard.KEY_1:
		        	mLiquid.setDrawMode(Liquid.DRAW_PARTICLES);
		        	break;
		        case Keyboard.KEY_2:
		        	mLiquid.setDrawMode(Liquid.DRAW_SURFACE);
		        	break;
		        case Keyboard.KEY_3:
		        	mLiquid.setDrawMode(Liquid.DRAW_SURFACE_REFLECTED);
		        	break;
		        case Keyboard.KEY_4:
		        	mLiquid.setDrawMode(Liquid.DRAW_TRIANGLES);
		        	break;
		        
				case Keyboard.KEY_RETURN:
					if (!mLiquid.isStarted()) {
						mLiquid.setIsStarted(true);
						synchronized (mLiquid) {
							mLiquid.notify();
						}

					} else {
						mLiquid.setIsStarted(false);
					}
					break;
				
				case Keyboard.KEY_G:
					Liquid.setGravity((Vector3f) Liquid.gravity().scale(-1f));
		        }
		    }
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
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
	}

	
	@Override
	protected void init(){
		super.init();
		glEnable(GL_DEPTH_TEST);
		//Setup cube for particles
		mParticleModel = Shapes.setupCube();
		mSphereModel = Shapes.setupSphere(1,20,20);
		mGlassModel = Shapes.setupGlass();
		mFloorModel = Shapes.setupSquare();
		Debug.println("CUBE SETUP", Debug.MAX_DEBUG);
		
		setupLight();
		Debug.println("LIGHT SETUP", Debug.MAX_DEBUG);
		
		mSkybox = new Skybox();
		mSkybox.init(mCamera.getProjectionMatrix());
		
		//Upload projectionMatrix to shaders
		glUseProgram(particleProgram);
		FloatBuffer projectionMatrixBuffer = GLUtils.matrix4Buffer(mCamera.getProjectionMatrix());
		glUniformMatrix4(glGetUniformLocation(particleProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		
		glUseProgram(surfaceProgram);
		glUniformMatrix4(glGetUniformLocation(surfaceProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		uploadLightToShader(surfaceProgram);
		
		glUseProgram(reflectionProgram);
		glUniformMatrix4(glGetUniformLocation(reflectionProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		uploadLightToShader(reflectionProgram);
		
		glUseProgram(modelProgram);
		glUniformMatrix4(glGetUniformLocation(modelProgram,"projectionMatrix"), true, projectionMatrixBuffer);
		uploadLightToShader(modelProgram);
	}

}
