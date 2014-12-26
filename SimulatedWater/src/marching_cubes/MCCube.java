package marching_cubes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.lwjgl.util.vector.Vector3f;

import Utils.MathUtils;
import data_types.Particle;

/**
 * MCCube class Implements the marching cubes algorithm
 *
 */
public class MCCube {
	public static final int LEFT = -1;
	public static final int RIGHT = 1;
	public static final int DOWN = -2;
	public static final int UP = 2;
	public static final int IN = -3;
	public static final int OUT = 3;
	public static final int SELF = 0;
	
	public static final float EPSILON = 0.00001f;

	private Vector3f mPos; // Middle of the cube
	private MCVoxel[] mVoxels = new MCVoxel[8];
	//private Vector3f[] mValueVectors = new Vector3f[8];

	private static float mScale;
	private int mId;

	private HashMap<Integer, MCCube> mNeighbours;
	private Vector<Particle> mParticleList;
	private Vector<MCCube> mCubesInRange;
	private Vector<MCVoxel> mVoxelsInRange;

	public MCCube() {

	}

	/**
	 * Initialize cube from position of corner 0
	 * 
	 * @param pos
	 */
	public MCCube(Vector3f pos, float scale, int id) {
		mId = id; // Cube ID
		mPos = pos; // Middle of cube
		
		// Vertex positions
		// Bottom
		mVoxels[0] = new MCVoxel(pos.x - scale / 2, pos.y - scale / 2, pos.z + scale / 2,0);
		mVoxels[1] = new MCVoxel(pos.x + scale / 2, pos.y - scale / 2, pos.z + scale / 2,0);
		mVoxels[2] = new MCVoxel(pos.x + scale / 2, pos.y - scale / 2, pos.z - scale / 2,0);
		mVoxels[3] = new MCVoxel(pos.x - scale / 2, pos.y - scale / 2, pos.z - scale / 2,0);
		
		//Top
		mVoxels[4] = new MCVoxel(pos.x - scale / 2, pos.y + scale / 2, pos.z + scale / 2,0);
		mVoxels[5] = new MCVoxel(pos.x + scale / 2, pos.y + scale / 2, pos.z + scale / 2,0);
		mVoxels[6] = new MCVoxel(pos.x + scale / 2, pos.y + scale / 2, pos.z - scale / 2,0);
		mVoxels[7] = new MCVoxel(pos.x - scale / 2, pos.y + scale / 2, pos.z - scale / 2,0);
	
		// Normals
		// Bottom
		mVoxels[0].setNormal(new Vector3f(-1, -1, -1));
		mVoxels[1].setNormal(new Vector3f(1, -1, -1));
		mVoxels[2].setNormal(new Vector3f(1, -1, 1));
		mVoxels[3].setNormal(new Vector3f(-1, -1, 1));

		// Top
		mVoxels[4].setNormal(new Vector3f(-1, 1, -1));
		mVoxels[5].setNormal(new Vector3f(1, 1, -1));
		mVoxels[6].setNormal(new Vector3f(1, 1, 1));
		mVoxels[7].setNormal(new Vector3f(-1, 1, 1));

		mScale = scale;
		mId = id;
		mParticleList = new Vector<Particle>();
		mNeighbours = new HashMap<Integer, MCCube>();
		mVoxelsInRange = new Vector<MCVoxel>();
		mCubesInRange = new Vector<MCCube>();

	}
	
	/**
	 * Clear particles in this cube
	 */
	public synchronized void resetParticleList() {
		mParticleList.clear();
	}

	/**
	 * Add a particle to this cube
	 * @param particle Particle to add
	 */
	public synchronized void addParticle(Particle particle) {
		mParticleList.add(particle);
	}

	/**
	 * Remove a particle from the list of this cube
	 * @param particle Particle to remove
	 */
	public synchronized void removeParticle(Particle particle) {
		mParticleList.remove(particle);
	}

	/**
	 * Check if a particle is inside the bounds of this cube
	 * @param particle to check
	 * @return true if inside bounds, false if not
	 */
	public synchronized boolean containsParticle(Particle particle) {
		if (	((particle.getPosition().x <= mPos.x + mScale / 2 + EPSILON) && (particle.getPosition().x >= mPos.x - mScale / 2 -EPSILON)) &&
				((particle.getPosition().y <= mPos.y + mScale / 2+ EPSILON) && (particle.getPosition().y >= mPos.y - mScale / 2- EPSILON)) &&
				((particle.getPosition().z <= mPos.z + mScale / 2+ EPSILON) && (particle.getPosition().z >= mPos.z - mScale / 2- EPSILON))) {
			return true;
		} else
			return false;
	}

	/**
	 * Get a map of the neighbours of this cube
	 */
	public synchronized HashMap<Integer, MCCube> getNeighbours() {
		return mNeighbours;
	}

	/**
	 * Add a neighbour in a direction
	 * 
	 * @param direction
	 * @param cube
	 */
	public void addNeighbour(int direction, MCCube cube) {
		mNeighbours.put(direction, cube);
	}

	/**
	 * Add neighbours to this cube by checking the positions of the other cubes
	 * 
	 * @param allCubes
	 */
	public void addNeighbours(Vector<MCCube> allCubes) {
		for (MCCube cube : allCubes) {
			for (int dir = -3; dir < 4; dir++) {
				if(mNeighbours.containsKey(dir) || dir == MCCube.SELF){
					//Already has this neighbour or is self
					continue;
				}
				
				Vector3f temp = Vector3f.add(mPos, vecInDir(dir), null);
				temp = MathUtils.toDecimals(temp, 4);
				if (MathUtils.isEqual(cube.getPosition(), temp)) {
					this.addNeighbour(dir, cube);
					cube.addNeighbour(-dir, this);
					shareVoxels(cube,dir);
				}
			}
		}
	}
	
	public void shareVoxels(MCCube cube,int dir){
		switch(dir){
		case MCCube.LEFT:
			cube.setVoxel(1,mVoxels[0]);
			cube.setVoxel(2,mVoxels[3]);
			cube.setVoxel(5,mVoxels[4]);
			cube.setVoxel(6,mVoxels[7]);
			break;
		case MCCube.RIGHT:
			cube.setVoxel(0,mVoxels[1]);
			cube.setVoxel(3,mVoxels[2]);
			cube.setVoxel(4,mVoxels[5]);
			cube.setVoxel(7,mVoxels[6]);
			break;
		case MCCube.DOWN:
			cube.setVoxel(0,mVoxels[4]);
			cube.setVoxel(1,mVoxels[5]);
			cube.setVoxel(2,mVoxels[6]);
			cube.setVoxel(3,mVoxels[7]);
			break;
		case MCCube.UP:
			cube.setVoxel(4,mVoxels[0]);
			cube.setVoxel(5,mVoxels[1]);
			cube.setVoxel(6,mVoxels[2]);
			cube.setVoxel(7,mVoxels[3]);
			break;
		case MCCube.IN:
			cube.setVoxel(0,mVoxels[3]);
			cube.setVoxel(4,mVoxels[7]);
			cube.setVoxel(5,mVoxels[6]);
			cube.setVoxel(1,mVoxels[2]);
			break;
		case MCCube.OUT:
			cube.setVoxel(3,mVoxels[0]);
			cube.setVoxel(7,mVoxels[4]);
			cube.setVoxel(6,mVoxels[5]);
			cube.setVoxel(2,mVoxels[1]);
			break;
		}
	}
	
	/**
	 * Set a corner voxel to another voxel by reference.
	 * @param i
	 * @param voxel
	 */
	public void setVoxel(int i,MCVoxel voxel){
		mVoxels[i] = voxel;
	}

	/**
	 * Locate the cube in a direction according to direction
	 * 
	 * @param dir
	 * @return
	 */
	public static Vector3f vecInDir(int dir) {
		Vector3f temp = new Vector3f();
		switch (dir) {
		case LEFT:
			temp.set(-mScale, 0, 0);
			break;
		case RIGHT:
			temp.set(mScale, 0, 0);
			break;
		case DOWN:
			temp.set(0, -mScale, 0);
			break;
		case UP:
			temp.set(0, mScale, 0);
			break;
		case IN:
			temp.set(0, 0, -mScale);
			break;
		case OUT:
			temp.set(0, 0, mScale);
			break;
		default:
			break;
		}
		return temp;
	}

	/**
	 * Get the position of the cube
	 */
	public Vector3f getPosition() {
		return mPos;
	}

	/**
	 * Get the side length of the cube
	 */
	public float getSize() {
		return mScale;
	}

	/**
	 * Get the cube id
	 * 
	 * @return cube id
	 */
	public int getId() {
		return mId;
	}
	
	public void resetVoxelValues(){
		for(int i = 0; i<8;i++){
			mVoxels[i].resetValue();
		}
	}
	
	/**
	 * Get position of corner voxel
	 * @param i
	 * @return
	 */
	public Vector3f getPosition(int i){
		return mVoxels[i].getPosition();
	}
	
	/**
	 * Get voxel in corner i
	 * @param i corner i
	 * @return the corner voxel
	 */
	public MCVoxel getVoxel(int i){
		if(i<0 || i>7){
			return null;
		}
		return mVoxels[i];
	}
	
	/**
	 * Adds to value
	 * @param i
	 * @param val
	 */
	public void addToValue(int i,float val){
		mVoxels[i].addValue(val);
	}

	
	/**
	 * Get the nearby cubes inside a range Calculates by 6-agecensy
	 * 
	 * @param range
	 * @return
	 */
	private Vector<MCCube> getCubesInRange(int range) {
		Vector<MCCube> cubesInRange = new Vector<MCCube>();
		getCubesInRangeHelper(range, 0, cubesInRange);
		return cubesInRange;
	}

	/**
	 * Helper function for the get cubes in range function
	 * 
	 * @param range
	 * @param fromDir
	 * @param cubesInRange
	 */
	private void getCubesInRangeHelper(int range, int fromDir, Vector<MCCube> cubesInRange) {
		
		if (range == 0)
			return;
		int count = range;
		count--;

		if (!cubesInRange.contains(this)) {
			cubesInRange.add(this);
		}

		for (int dir = -3; dir < 4; dir++) {
			// Is the direction 0 or the direction of the neighbour function was called from
			if (dir == MCCube.SELF || dir == fromDir) {
				continue;
			}
			else if (!mNeighbours.containsKey(dir) ||
					(cubesInRange.contains(mNeighbours.get(dir)))) {
				// Check if there is any neighbouring cube in this direction
				continue;
			} 

			MCCube nextCube = mNeighbours.get(dir);
			nextCube.getCubesInRangeHelper(count, -dir, cubesInRange);
		}
	}

	/**
	 * Get particles in the cube
	 * 
	 * @return
	 */
	public synchronized Vector<Particle> getParticles() {
		return mParticleList;
	}
	
//	private void calculateVoxelValues(){
//		for(int i = 0;i<8;i++){
//			mVoxels[i].addValue(mValueVectors[i].length());
//		}
//	}

	/**
	 * Perform marching cube on single grid cube
	 * 
	 * @param isoLevel
	 * @param fScale
	 * @param triangles
	 * @param indices
	 * @param normals
	 * @return
	 */
	Vector<MCTriangle> march(float isoLevel, float fScale, ArrayList<Float> vertexPositions, ArrayList<Short> indices,
			ArrayList<Float> normals) {
		int cubeindex = 0;
		int i;
		//calculateVoxelValues();
		
		Vector3f[] vertlist = new Vector3f[12];
		Vector3f[] normlist = new Vector3f[12];
		
		if (mVoxels[0].getValue() < isoLevel)
			cubeindex |= 1;
		if (mVoxels[1].getValue() < isoLevel)
			cubeindex |= 2;
		if (mVoxels[2].getValue() < isoLevel)
			cubeindex |= 4;
		if (mVoxels[3].getValue() < isoLevel)
			cubeindex |= 8;
		if (mVoxels[4].getValue() < isoLevel)
			cubeindex |= 16;
		if (mVoxels[5].getValue() < isoLevel)
			cubeindex |= 32;
		if (mVoxels[6].getValue() < isoLevel)
			cubeindex |= 64;
		if (mVoxels[7].getValue() < isoLevel)
			cubeindex |= 128;

		/* Cube is entirely in/out of the surface */
		if (MCGrid.edgeTable[cubeindex] == 0)
			return null;

		Vector3f[] result = new Vector3f[2];
		/* Find the vertices where the surface intersects the cube */
		if ((MCGrid.edgeTable[cubeindex] & 1) > 0){
			result = interpolate(isoLevel, mVoxels[0], mVoxels[1]);
			vertlist[0] = result[0];
			normlist[0] = result[1];
		}	
		if ((MCGrid.edgeTable[cubeindex] & 2) > 0){
			result = interpolate(isoLevel, mVoxels[1], mVoxels[2]);
			vertlist[1] = result[0];
			normlist[1] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 4) > 0){
			result = interpolate(isoLevel, mVoxels[2], mVoxels[3]);
			vertlist[2] = result[0];
			normlist[2] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 8) > 0){
			result = interpolate(isoLevel, mVoxels[3], mVoxels[0]);
			vertlist[3] = result[0];
			normlist[3] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 16) > 0){
			result = interpolate(isoLevel, mVoxels[4], mVoxels[5]);
			vertlist[4] = result[0];
			normlist[4] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 32) > 0){
			result = interpolate(isoLevel, mVoxels[5], mVoxels[6]);
			vertlist[5] = result[0];
			normlist[5] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 64) > 0){
			result = interpolate(isoLevel, mVoxels[6], mVoxels[7]);
			vertlist[6] = result[0];
			normlist[6] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 128) > 0){
			result = interpolate(isoLevel, mVoxels[7], mVoxels[4]);
			vertlist[7] = result[0];
			normlist[7] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 256) > 0){
			result = interpolate(isoLevel, mVoxels[0], mVoxels[4]);
			vertlist[8] = result[0];
			normlist[8] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 512) > 0){
			result = interpolate(isoLevel, mVoxels[1], mVoxels[5]);
			vertlist[9] = result[0];
			normlist[9] = result[1];
		} 
		if ((MCGrid.edgeTable[cubeindex] & 1024) > 0){
			result = interpolate(isoLevel, mVoxels[2], mVoxels[6]);
			vertlist[10] = result[0];
			normlist[10] = result[1];
		}
		if ((MCGrid.edgeTable[cubeindex] & 2048) > 0){
			result = interpolate(isoLevel, mVoxels[3], mVoxels[7]);
			vertlist[11] = result[0];
			normlist[11] = result[1];
		}

		Vector<MCTriangle> triangles = new Vector<MCTriangle>();
		for (i = 0; MCGrid.triTable[cubeindex][i] != -1; i += 3) {
			if (vertexPositions != null) {
				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i]].x);
				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i]].y);
				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i]].z);

				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i + 1]].x);
				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i + 1]].y);
				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i + 1]].z);

				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i + 2]].x);
				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i + 2]].y);
				vertexPositions.add(vertlist[MCGrid.triTable[cubeindex][i + 2]].z);
			}
			
			if (indices != null) {
				indices.add((short) indices.size());
				indices.add((short) indices.size());
				indices.add((short) indices.size());
			}
			
			if(normals != null){
				normals.add(normlist[MCGrid.triTable[cubeindex][i]].x);
				normals.add(normlist[MCGrid.triTable[cubeindex][i]].y);
				normals.add(normlist[MCGrid.triTable[cubeindex][i]].z);

				normals.add(normlist[MCGrid.triTable[cubeindex][i + 1]].x);
				normals.add(normlist[MCGrid.triTable[cubeindex][i + 1]].y);
				normals.add(normlist[MCGrid.triTable[cubeindex][i + 1]].z);

				normals.add(normlist[MCGrid.triTable[cubeindex][i + 2]].x);
				normals.add(normlist[MCGrid.triTable[cubeindex][i + 2]].y);
				normals.add(normlist[MCGrid.triTable[cubeindex][i + 2]].z);
			}

			MCTriangle temp = new MCTriangle(
					vertlist[MCGrid.triTable[cubeindex][i]],
					vertlist[MCGrid.triTable[cubeindex][i + 1]],
					vertlist[MCGrid.triTable[cubeindex][i + 2]],
					normlist[MCGrid.triTable[cubeindex][i]],
					normlist[MCGrid.triTable[cubeindex][i + 1]],
					normlist[MCGrid.triTable[cubeindex][i + 2]]);
			triangles.add(temp);
		}
		return triangles;
	}

	/**
	 * Linearly interpolate the position where an isosurface cuts an edge
	 * between two vertices, each with their own scalar value
	 * @param isolevel
	 * @param v1
	 * @param v2
	 * @param n1
	 * @param n2
	 * @return
	 */
	Vector3f[] interpolate(double isolevel, MCVoxel v1, MCVoxel v2) {
		Vector3f result[] = new Vector3f[2];
		Vector3f p1 = v1.getPosition();
		Vector3f p2 = v2.getPosition();
		Vector3f n1 = v1.getNormal();
		Vector3f n2 = v2.getNormal();
		double valp1 = v1.getValue();
		double valp2 = v2.getValue();
		
		double mu;
		Vector3f p = new Vector3f();
		Vector3f n = new Vector3f();

		if (Math.abs(isolevel - valp1) < EPSILON){
			result[0] = p1;
			result[1] = n1;//(Vector3f) Vector3f.add(n1, n2, null).scale((float) 0.5);
			return result;
		}
		else if (Math.abs(isolevel - valp2) < EPSILON){
			result[0] = p2;
			result[1] = n2;//(Vector3f) Vector3f.add(n1, n2, null).scale((float) 0.5);
			return result;
		}
		else if (Math.abs(valp1 - valp2) < EPSILON){
			result[0] = p1;
			result[1] = n1;//(Vector3f) Vector3f.add(n1, n2, null).scale((float) 0.5);
			return result;
		}
		mu = (isolevel - valp1) / (valp2 - valp1);
		p.x = (float) (p1.x + mu * (p2.x - p1.x));
		p.y = (float) (p1.y + mu * (p2.y - p1.y));
		p.z = (float) (p1.z + mu * (p2.z - p1.z));
		result[0] = p;
		
		n.x = (float) (n1.x + mu * (n2.x - n1.x));
		n.y = (float) (n1.y + mu * (n2.y - n1.y));
		n.z = (float) (n1.z + mu * (n2.z - n1.z));
		n.normalise();
		result[1] = n;
		
		return result;
	}
	
	/**
	 * Get the voxels of this cube
	 * @return
	 */
	public MCVoxel[] getVoxels(){
		return mVoxels;
	}
	
	/**
	 * Calculates nearby cubes based on 6-agacency
	 * @param i
	 */
	public void calculateNearbyCubes(int i) {
		mCubesInRange = getCubesInRange(i);
		for(MCCube cube: mCubesInRange){
			for(MCVoxel voxel : cube.getVoxels()){
				if (mVoxelsInRange.contains(voxel))
					continue;
				else
					mVoxelsInRange.add(voxel);
			}
		}
	}

	/**
	 * Get voxels in nearby range of this cube
	 * @return
	 */
	public synchronized Vector<MCVoxel> getNearbyVoxels(){
		return mVoxelsInRange;
	}
	
	/**
	 * Get neighbourhood of MCCubes
	 * @return
	 */
	public synchronized Vector<MCCube> getNearbyCubes(){
		return mCubesInRange;
	}

	/**
	 * Calculates neighbourhood of particle. within range
	 * @param grid The grid which this MCCube is in
	 * @param range 0,1,2,3,....
	 */
	public void calcNeighbourhood(MCGrid grid, int range) {
		int sideLength = (int) grid.getSideLength();
		int cubeId = mId;
		for (int x = -range; x < range + 1; x++) {
			for (int y = -range; y < range + 1; y++) {
				for (int z = -range; z < range + 1; z++) {
					cubeId = (int) (mId + x + y * sideLength + z * Math.pow(sideLength, 2));

					if (cubeId >= 0 && cubeId < grid.getNumberOfCubes()) {
						MCCube cube = grid.getCubes().get(cubeId);
						float distance = Vector3f.sub(mPos, cube.getPosition(), null).length();

						if (MathUtils.toDecimals(distance, 2) <= MathUtils.toDecimals(
								(float) Math.sqrt(3 * Math.pow(mScale, 2)), 2)) {
							mCubesInRange.add(cube);
							
							for (MCVoxel voxel : cube.getVoxels()) {
								if (mVoxelsInRange.contains(voxel))
									continue;
								else
									mVoxelsInRange.add(voxel);
							}

						}

					}

				}
			}
		}
		
	}
}
