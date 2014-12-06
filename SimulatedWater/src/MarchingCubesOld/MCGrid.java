package MarchingCubesOld;

import java.util.Vector;

import org.lwjgl.util.vector.Vector3f;

public class MCGrid {
	public static int[] directions = {-3,-2,-1,1,2,3};
	
	//Member variables
	private Vector<MCCube> mCubes;
	
	private Vector<MCVertex> mVertexes;
	
	/**
	 * Constructor
	 */
	public MCGrid(){
		mCubes = new Vector<MCCube>();
		
		//First cube
		MCCube c1 = new MCCube(1);
		mCubes.add(c1);
	}
	
	/**
	 * Get list of cubes
	 * @return
	 */
	public Vector<MCCube> getCubes(){
		return mCubes;
	}
	
	/**
	 * Set grid cubes
	 * @param cubes
	 */
	public void setCubes(Vector<MCCube> cubes){
		mCubes = cubes;
	}
	
	/**
	 * Create a cube adjacent to another cube
	 * @param adjacentCube
	 * @param dir
	 * @param id
	 */
	public void addCube(MCCube adjacentCube, int dir, int id){
		MCCube newCube = new MCCube(id);
		mCubes.add(newCube);
		
		newCube.setPos(Vector3f.add(adjacentCube.getPos(), MCCube.vecInDir(dir), null));
	
		//Check existing neighbors and link vertexes
		for(int direction:directions){
			Vector3f dummyPos = new Vector3f(newCube.getPos());
			dummyPos = Vector3f.add(dummyPos, MCCube.vecInDir(direction), null);
			
			for(int i = 0; i<mCubes.size();i++){
				MCCube someCube = mCubes.elementAt(i);
				if(someCube.getPos().equals(dummyPos)){
					switch(direction){
						case MCCube.LEFT:
							newCube.setCornerVertex(0,someCube.getCornerVertex(1));
							newCube.setCornerVertex(4,someCube.getCornerVertex(5));
							newCube.setCornerVertex(7,someCube.getCornerVertex(6));
							newCube.setCornerVertex(3,someCube.getCornerVertex(2));
						case MCCube.RIGHT:
							newCube.setCornerVertex(1,someCube.getCornerVertex(0));
							newCube.setCornerVertex(5,someCube.getCornerVertex(4));
							newCube.setCornerVertex(6,someCube.getCornerVertex(7));
							newCube.setCornerVertex(2,someCube.getCornerVertex(3));
						case MCCube.DOWN:
							newCube.setCornerVertex(1,someCube.getCornerVertex(2));
							newCube.setCornerVertex(5,someCube.getCornerVertex(6));
							newCube.setCornerVertex(4,someCube.getCornerVertex(7));
							newCube.setCornerVertex(0,someCube.getCornerVertex(3));
						case MCCube.UP:
							newCube.setCornerVertex(2,someCube.getCornerVertex(1));
							newCube.setCornerVertex(6,someCube.getCornerVertex(5));
							newCube.setCornerVertex(7,someCube.getCornerVertex(4));
							newCube.setCornerVertex(3,someCube.getCornerVertex(0));
						case MCCube.IN:
							newCube.setCornerVertex(0,someCube.getCornerVertex(4));
							newCube.setCornerVertex(1,someCube.getCornerVertex(5));
							newCube.setCornerVertex(2,someCube.getCornerVertex(6));
							newCube.setCornerVertex(3,someCube.getCornerVertex(7));
						case MCCube.OUT:
							newCube.setCornerVertex(4,someCube.getCornerVertex(0));
							newCube.setCornerVertex(5,someCube.getCornerVertex(1));
							newCube.setCornerVertex(6,someCube.getCornerVertex(2));
							newCube.setCornerVertex(7,someCube.getCornerVertex(3));
					}
					newCube.addNeighbor(direction, someCube);
					someCube.addNeighbor(-direction, newCube);
				}
			}
		}
		newCube.computeEdges();
	}
	

}
