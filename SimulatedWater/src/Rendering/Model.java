package Rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;

/**
 * 
 * 
 *
 */
public class Model {
	private float[] mVertexArray;
	private float[] mNormalArray;
	private float[] mTexCoordArray;
	private float[] mColorArray;
	private short[] mIndexArray;
	int mNumVertices;
	int mNumIndices;
	// Space for saving VBO and VAO IDs
	private int mVao; // VAO
	private int mVb, mIb, mNb, mTb; // VBOs

	/**
	 * Constructor
	 * 
	 * @param vertices
	 * @param normals
	 * @param texCoords
	 * @param colors
	 * @param indices
	 * @param numVert
	 * @param numInd
	 */
	public Model(float[] vertices, float[] normals, float[] texCoords, float[] colors, short[] indices) {
		set(vertices, normals, texCoords, normals, indices);
	}
	
	/**
	 * Standard constructor
	 */
	public Model() {
	}
	
	/**
	 * Set the model from input arrays
	 * @param vertices
	 * @param normals
	 * @param texCoords
	 * @param colors
	 * @param indices
	 */
	public void set(float[] vertices, float[] normals, float[] texCoords, float[] colors, short[] indices) {
		mVertexArray = vertices;
		mTexCoordArray = texCoords;
		mNormalArray = normals;
		mIndexArray = indices;
		mNumVertices = vertices.length;
		mNumIndices = indices.length;

		BuildModelVAO();
	}

	/**
	 * Binds opengl buffers from model variables
	 */
	private void BuildModelVAO() {
		mVao = glGenVertexArrays();

		mVb = glGenBuffers();
		mIb = glGenBuffers();
		mNb = glGenBuffers();

		if (mTexCoordArray != null) {
			mTb = glGenBuffers();
		}
		glBindVertexArray(mVao);

		// VBO for vertex data
		glBindBuffer(GL_ARRAY_BUFFER, mVb);

		// c: glBufferData(GL_ARRAY_BUFFER, mNumVertices*3*4, mVertexArray,
		// GL_STATIC_DRAW);
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(mVertexArray.length);
		vertexBuffer.put(mVertexArray);
		vertexBuffer.flip();
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

		// Commented outin Ingemars version
		// glVertexAttribPointer(glGetAttribLocation(program,
		// vertexVariableName), 3, GL_FLOAT, GL_FALSE, 0, 0);
		// glEnableVertexAttribArray(glGetAttribLocation(program,
		// vertexVariableName));

		// VBO for normal data
		if (mNormalArray != null) {
			glBindBuffer(GL_ARRAY_BUFFER, mNb);
			// c: glBufferData(GL_ARRAY_BUFFER,
			// m->numVertices*3*sizeof(GLfloat), m->normalArray,
			// GL_STATIC_DRAW);
			FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(mNormalArray.length);
			normalBuffer.put(mNormalArray);
			normalBuffer.flip();
			glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
		}

		// Commented out in Ingemars version

		// VBO for texture coordinate data NEW for 5b
		if (mTexCoordArray != null)
		{
			glBindBuffer(GL_ARRAY_BUFFER, mTb);
			// c: glBufferData(GL_ARRAY_BUFFER,
			// m->numVertices*2*sizeof(GLfloat), m->texCoordArray,
			// GL_STATIC_DRAW);
			FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(mTexCoordArray.length);
			textureBuffer.put(mTexCoordArray);
			textureBuffer.flip();
			glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
		}

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIb);
		ShortBuffer indexBuffer = BufferUtils.createShortBuffer(mIndexArray.length);
		//ByteBuffer indexBuffer = BufferUtils.createByteBuffer(mIndexArray.length);
		indexBuffer.put(mIndexArray);
		indexBuffer.flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
	}

	/**
	 * Draws the model
	 * Uploads to shader
	 * @param program
	 * @param vertexVariableName
	 * @param normalVariableName
	 * @param texCoordVariableName
	 */
	public void draw(int program, String vertexVariableName, String normalVariableName, String texCoordVariableName) {
		int loc;
		glBindVertexArray(mVao);
		glBindBuffer(GL_ARRAY_BUFFER, mVb);
		// System.err.println("DrawModel error: 1");

		loc = glGetAttribLocation(program, vertexVariableName);

		if (loc >= 0) {
			glVertexAttribPointer(loc, 3, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(loc);
		}
		else {
			System.err.println("DrawModel error: " + vertexVariableName + " not found in shader");
		}

		if (normalVariableName != null) {
			loc = glGetAttribLocation(program, normalVariableName);
			if (loc >= 0) {
				glBindBuffer(GL_ARRAY_BUFFER, mNb);
				glVertexAttribPointer(loc, 3, GL_FLOAT, false, 0, 0);
				glEnableVertexAttribArray(loc);
			}
			else {
				System.err.println("DrawModel error: " + normalVariableName + " not found in shader");
			}
		}

		if ((mTexCoordArray != null) && (texCoordVariableName != null)) {
			loc = glGetAttribLocation(program, texCoordVariableName);
			if (loc >= 0) {
				glBindBuffer(GL_ARRAY_BUFFER, mTb);
				glVertexAttribPointer(loc, 2, GL_FLOAT, false, 0, 0);
				glEnableVertexAttribArray(loc);
			}
			else {
				System.err.println("DrawModel error: " + texCoordVariableName + " not found in shader");
			}
		}
		// Bind to the index VBO that has all the information about the order of
		// the vertices
		// glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIb);
		glDrawElements(GL_TRIANGLES, mNumIndices, GL_UNSIGNED_SHORT, 0);
	}
	
	/**
	 * Allocates buffers
	 */
	public void clear(){
		glDeleteVertexArrays(mVao);
		glDeleteBuffers(mVb);
		glDeleteBuffers(mIb);
		glDeleteBuffers(mNb);
		glDeleteBuffers(mTb);
		
		mVertexArray = null;
		mNormalArray = null;
		mTexCoordArray = null;
		mIndexArray = null;
	}

}
