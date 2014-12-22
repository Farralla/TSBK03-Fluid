package rendering;

/**
 * Implements functionality to generate vertices of simple shapes
 *
 */
public class Shapes {
	private static final float PI = (float) Math.PI;

	/**
	 * Sets up a 3d cube
	 */
	public static Model setupCube() {

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

		Model model = new Model(vertices, null, null, null, indices);
		return model;
	}
	
	/**
	 * Sets up a 3d cube
	 */
	public static Model setupGlass() {

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

		Model model = new Model(vertices, normals, null, null, indices);
		return model;
	}

	/**
	 * Sets up a square
	 */
	public static Model setupSquare() {

		// The 4 vertices (corners of a particle)
		final float[] vertices = {
				// Front face
				-0.5f, 0f, -0.5f,
				0.5f, 0f, -0.5f,
				0.5f, 0f, 0.5f,
				-0.5f, 0f, 0.5f,
		};
		
		final float[] normals = {
				// Front face
				-0.5f, 1f, -0.5f,
				0.5f, 1f, -0.5f,
				0.5f, 1f, 0.5f,
				-0.5f, 1f, 0.5f,
		};

		final short[] indices = {
				0, 1, 2,
				2, 3, 0
		};

		Model model = new Model(vertices, normals, null, null, indices);
		return model;
	}
	
	/**
	 * Creates a sphere model
	 * Implementation from github user jfbaraton:
	 * https://github.com/SpaceAppsChallengeToulouse/StarlightTlse/blob/master/Rajawali/src/rajawali/primitives/Sphere.java
	 * @param radius
	 * @param segmentsW
	 * @param segmentsH
	 * @return
	 */
	public static Model setupSphere(float radius, int segmentsW, int segmentsH) {
		int numVertices = (segmentsW + 1) * (segmentsH + 1);
		int numIndices = 2 * segmentsW * (segmentsH - 1) * 3;

		float[] vertices = new float[numVertices * 3];
		float[] normals = new float[numVertices * 3];
		float[] colors = new float[numVertices * 4];
		short[] indices = new short[numIndices];

		int i, j;
		int vertIndex = 0, index = 0;
		final float normLen = 1.0f / radius;

		for (j = 0; j <= segmentsH; ++j) {
			float horAngle = (PI * j / segmentsH);
			float z = radius * (float) Math.cos(horAngle);
			float ringRadius = radius * (float) Math.sin(horAngle);

			for (i = 0; i <= segmentsW; ++i) {
				float verAngle = 2.0f * PI * i / segmentsW;
				float x = ringRadius * (float) Math.cos(verAngle);
				float y = ringRadius * (float) Math.sin(verAngle);

				normals[vertIndex] = x * normLen;
				vertices[vertIndex++] = x;
				normals[vertIndex] = z * normLen;
				vertices[vertIndex++] = z;
				normals[vertIndex] = y * normLen;
				vertices[vertIndex++] = y;

				if (i > 0 && j > 0) {
					int a = (segmentsW + 1) * j + i;
					int b = (segmentsW + 1) * j + i - 1;
					int c = (segmentsW + 1) * (j - 1) + i - 1;
					int d = (segmentsW + 1) * (j - 1) + i;

					if (j == segmentsH) {
						indices[index++] = (short) a;
						indices[index++] = (short)c;
						indices[index++] = (short)d;
					} else if (j == 1) {
						indices[index++] = (short)a;
						indices[index++] = (short)b;
						indices[index++] = (short)c;
					} else {
						indices[index++] = (short)a;
						indices[index++] = (short)b;
						indices[index++] = (short)c;
						indices[index++] = (short)a;
						indices[index++] = (short)c;
						indices[index++] = (short)d;
					}
				}
			}
		}

		int numUvs = (segmentsH + 1) * (segmentsW + 1) * 2;
		float[] textureCoords = new float[numUvs];

		numUvs = 0;
		for (j = 0; j <= segmentsH; ++j) {
			for (i = 0; i <= segmentsW; ++i) {
				textureCoords[numUvs++] = -(float) i / segmentsW;
				textureCoords[numUvs++] = (float) j / segmentsH;
			}
		}

		int numColors = numVertices * 4;
		for (j = 0; j < numColors; j += 4)
		{
			colors[j] = 1.0f;
			colors[j + 1] = 0;
			colors[j + 2] = 0;
			colors[j + 3] = 1.0f;
		}
		
		Model model = new Model(vertices,normals,null,null,indices);
		return model;
	}
	
}
