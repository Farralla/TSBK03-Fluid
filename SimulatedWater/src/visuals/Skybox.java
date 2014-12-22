package visuals;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Matrix4f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import rendering.Model;
import rendering.Shapes;
import Utils.Debug;
import Utils.GLUtils;
import Utils.MathUtils;

import com.badlogic.gdx.utils.BufferUtils;

/**
 * Skybox class:
 * Implements functionality to load textures and use these to form a cube map
 * 
 *
 */
public class Skybox {
	private Texture[] mTextures;
	private Model mModel;
	private int mTexUnit;

	public int program;

	/**
	 * Constructor
	 * @param ihow many sides
	 */
	public Skybox(int i) {
		program = GLUtils.loadShaders("src/shaders/skybox.vert", "src/shaders/skybox.frag");
		mModel = Shapes.setupCube();
		mTextures = new Texture[i];
		loadTextures("src/textures/skybox/");
		generateCubeMap();
	}

	/**
	 * Standard consturctor
	 * Generates skybox with 6 sides
	 */
	public Skybox() {
		this(6);
	}

	/**
	 * Loads the skybox textures
	 * @param folderPath
	 * @return
	 */
	public boolean loadTextures(String folderPath) {
		for (int i = 0; i < mTextures.length; i++) {
				//Debug.println( System.getProperty("user.dir"));
				Debug.println("Loading skybox texture: " + folderPath + "sky" + i +".png");
				try{
				mTextures[i] = TextureLoader.getTexture("PNG",ResourceLoader.getResourceAsStream(folderPath + "sky" + i + ".png"));
				} catch(IOException e){
					Debug.err(e.getMessage(), Debug.MAX_DEBUG);
				}
		}
		return true;
	}

	/**
	 * Init skybox by uploading to shader
	 * HAS TO BE CALLED FROM OPENGL CONTEXT
	 * 
	 */
	public void init(Matrix4f projectionMatrix) {
		glUseProgram(program);
		Matrix4f transSkybox = MathUtils.transMatrix(0, 0, 0);
		FloatBuffer transMatrixBuffer = GLUtils.matrix4Buffer(transSkybox);
		FloatBuffer projMatrixBuffer = GLUtils.matrix4Buffer(projectionMatrix);
		glUniform1i(glGetUniformLocation(program, "cubeMap"), 0); // Texture unit 0:
		glUniformMatrix4(glGetUniformLocation(program, "projectionMatrix"), true, projMatrixBuffer);
		glUniformMatrix4(glGetUniformLocation(program, "modelMatrix"), true, transMatrixBuffer);
	}

	/**
	 * Draws the skybox
	 * @param viewMatrix
	 */
	public void draw(Matrix4f viewMatrix) {
		glUseProgram(program);
		glDisable(GL_DEPTH_TEST);
		Matrix4f world2ViewSky = new Matrix4f(viewMatrix);
		world2ViewSky.m30 = 0;
		world2ViewSky.m31 = 0;
		world2ViewSky.m32 = 0;
		FloatBuffer viewMatrixBuffer = GLUtils.matrix4Buffer(world2ViewSky);
		glUniformMatrix4(glGetUniformLocation(program, "viewMatrix"), true, viewMatrixBuffer);
		mModel.draw(program, "in_Position", null, null);
		glEnable(GL_DEPTH_TEST);
	}

	public Texture[] getTextures() {
		return mTextures;
	}

	/**
	 * Generates the cubemap to which represents the skybox
	 */
	public void generateCubeMap(){
		mTexUnit = glGenTextures();
		glActiveTexture(GL_TEXTURE0);

		glBindTexture(GL_TEXTURE_CUBE_MAP, mTexUnit);
		ByteBuffer texData = BufferUtils.newByteBuffer(mTextures[0].getTextureData().length);
		texData.put(mTextures[0].getTextureData());
		texData.flip();
		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL_RGB, mTextures[0].getTextureWidth(),
				mTextures[0].getTextureHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, texData);
		
		texData = BufferUtils.newByteBuffer(mTextures[1].getTextureData().length);
		texData.put(mTextures[1].getTextureData());
		texData.flip();
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL_RGB, mTextures[1].getTextureWidth(),
				mTextures[1].getTextureHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, texData);
		
		texData = BufferUtils.newByteBuffer(mTextures[2].getTextureData().length);
		texData.put(mTextures[2].getTextureData());
		texData.flip();
		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL_RGB, mTextures[2].getTextureWidth(),
				mTextures[2].getTextureHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, texData);
		
		texData = BufferUtils.newByteBuffer(mTextures[3].getTextureData().length);
		texData.put(mTextures[3].getTextureData());
		texData.flip();
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL_RGB, mTextures[3].getTextureWidth(), 
				mTextures[3].getTextureHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, texData);
		
		texData = BufferUtils.newByteBuffer(mTextures[4].getTextureData().length);
		texData.put(mTextures[4].getTextureData());
		texData.flip();
		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL_RGB, mTextures[4].getTextureWidth(),
				mTextures[4].getTextureHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, texData);
		
		texData = BufferUtils.newByteBuffer(mTextures[5].getTextureData().length);
		texData.put(mTextures[5].getTextureData());
		texData.flip();
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL_RGB, mTextures[5].getTextureWidth(),
				mTextures[5].getTextureHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, texData);


		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);


//		glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
//		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR); // Linear Filtered
	}
}