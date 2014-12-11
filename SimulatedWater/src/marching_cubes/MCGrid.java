package marching_cubes;

import java.util.Vector;

import org.lwjgl.util.vector.Vector3f;

import Utils.Debug;
import Utils.MathUtils;

public class MCGrid {
	public static final float DEFAULT_GRID_SCALE = 0.03f;
	public static final float DEFAULT_ISO_LEVEL = 0.01f;
	
	private float mIsoLevel, mScale, mSize, mNumCubes;
	Vector<MCCube> mCubes;
	Vector<MCTriangle> mTriangles;
	
	public MCGrid(){
		mCubes = new Vector<MCCube>();
		mTriangles = new Vector<MCTriangle>();
	}
	
	public MCGrid(float size, float scale, float isoLevel){
		mCubes = new Vector<MCCube>();
		mTriangles = new Vector<MCTriangle>();
		init(size,scale,isoLevel);
		calculateNeighbourhoods();
		System.out.println("Created MCGrid");
	}
	
	/**
	 * Initialize grid with size and cube size scale
	 * @param size length of side in cubical mesh grid
	 * @param scale cube side length
	 */
	public void init(float size, float scale, float isoLevel){
		mIsoLevel = isoLevel;
		mScale = scale;
		mSize = size;
		
		int numBoxes = (int) (size/scale);
		
		int id = 0;
		for(int x = 0; x < numBoxes;x++){
			float xPos = MathUtils.toDecimals(x*scale + scale/2,4);
			for(int y = 0; y < numBoxes;y++){
				float yPos = MathUtils.toDecimals(y*scale + scale/2,4);
				for(int z = 0; z < numBoxes;z++){
					float zPos = MathUtils.toDecimals(z*scale + scale/2,4);
					Vector3f pos = new Vector3f(xPos,yPos,zPos);
					mCubes.add(new MCCube(pos,scale,id));
					id++;
				}
			}
		}
		mNumCubes = mCubes.size();
	}
	
	public void resetScalarField(){
		for(MCCube cube: mCubes){
			cube.resetValues();
		}
	}
	
	public void changeIsoLevel(String incrOrDecr){
		switch(incrOrDecr){
		case "increase":
			if(mIsoLevel>0.5)
				mIsoLevel += MathUtils.toDecimals(mIsoLevel/10, 1);
			else
				mIsoLevel += 0.05;
			break;
		case "decrease":
			if(mIsoLevel>0.5)
				mIsoLevel -= MathUtils.toDecimals(mIsoLevel/10, 1);
			else
				mIsoLevel -= 0.05;
		}

	}
	
	public float getIsoLevel(){
		return mIsoLevel;
	}
	
	public void calculateNeighbourhoods(){
		for(MCCube cube:mCubes){
			cube.addNeighbours(mCubes);
		}
	}
	
	/**
	 * Updates the scalar field
	 * Then polygonizes the grid
	 */
	public void update(){
		updateScalarField();
		march();
	}
	
	/**
	 * Update the entire scalar distance field
	 */
	public void updateScalarField(){
		for(MCCube cube:mCubes){
			cube.updateScalarField();
		}
		
	}
	
	/**
	 * Polygonise every cube in grid
	 */
	public synchronized void march(){
		synchronized(mTriangles){
			mTriangles.clear();
		}
		for(MCCube cube:mCubes){
			Vector<MCTriangle> newTriangles = new Vector<MCTriangle>();
			newTriangles = cube.march(mIsoLevel, mScale, null, null, null);
			if(newTriangles != null){
				synchronized(mTriangles){
					mTriangles.addAll(newTriangles);	
				}
				//Debug.println("added cube triangles", Debug.MAX_DEBUG);
			}
		}
	}
	
	/**
	 * Get all triangles in grid
	 * @return
	 */
	public synchronized Vector<MCTriangle> getTriangles(){
		return mTriangles;
	}
	
	/**
	 * Returns a vector with all MCCubes
	 * @return
	 */
	public synchronized Vector<MCCube> getCubes(){
		return mCubes;
	}
	
}
