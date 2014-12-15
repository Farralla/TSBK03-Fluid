package Rendering;

public class SphereCreator {

	private final float PI = (float) Math.PI;
	private float mRadius;
	private int mSegmentsW;
	private int mSegmentsH;
	
	private float[] mVertices;
	private float[] mNormals;
	private float[] mColors;
	private short[] mIndices;

	public SphereCreator(float radius, int segmentsW, int segmentsH) {
		mRadius = radius;
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		init();
	}

	private void init() {
		int numVertices = (mSegmentsW + 1) * (mSegmentsH + 1);
		int numIndices = 2 * mSegmentsW * (mSegmentsH - 1) * 3;

		float[] vertices = new float[numVertices * 3];
		float[] normals = new float[numVertices * 3];
		float[] colors = new float[numVertices * 4];
		short[] indices = new short[numIndices];

		int i, j;
		int vertIndex = 0, index = 0;
		final float normLen = 1.0f / mRadius;

		for (j = 0; j <= mSegmentsH; ++j) {
			float horAngle = PI * j / mSegmentsH;
			float z = mRadius * (float) Math.cos(horAngle);
			float ringRadius = mRadius * (float) Math.sin(horAngle);

			for (i = 0; i <= mSegmentsW; ++i) {
				float verAngle = 2.0f * PI * i / mSegmentsW;
				float x = ringRadius * (float) Math.cos(verAngle);
				float y = ringRadius * (float) Math.sin(verAngle);

				normals[vertIndex] = x * normLen;
				vertices[vertIndex++] = x;
				normals[vertIndex] = z * normLen;
				vertices[vertIndex++] = z;
				normals[vertIndex] = y * normLen;
				vertices[vertIndex++] = y;

				if (i > 0 && j > 0) {
					int a = (mSegmentsW + 1) * j + i;
					int b = (mSegmentsW + 1) * j + i - 1;
					int c = (mSegmentsW + 1) * (j - 1) + i - 1;
					int d = (mSegmentsW + 1) * (j - 1) + i;

					if (j == mSegmentsH) {
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

		int numUvs = (mSegmentsH + 1) * (mSegmentsW + 1) * 2;
		float[] textureCoords = new float[numUvs];

		numUvs = 0;
		for (j = 0; j <= mSegmentsH; ++j) {
			for (i = 0; i <= mSegmentsW; ++i) {
				textureCoords[numUvs++] = -(float) i / mSegmentsW;
				textureCoords[numUvs++] = (float) j / mSegmentsH;
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
		set(vertices,normals,indices,colors);
	}
	
	private void set(float[] vertices, float[] normals, short[] indices, float[] colors){
		mVertices = vertices;
		mNormals = normals;
		mIndices = indices;
		mColors = colors;
	}
	
	public float[] getVertices(){
		return mVertices;
	}
	
	public float[] getNormals(){
		return mNormals;
	}
	
	public short[] getIndices(){
		return mIndices;
	}
	
	public float[] getColors(){
		return mNormals;
	}
}
