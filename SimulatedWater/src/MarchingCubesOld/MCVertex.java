package MarchingCubesOld;

import org.lwjgl.util.vector.Vector3f;

public class MCVertex {
	private static int DEFAULT_WEIGHT = 0;
	
	public Vector3f pos;
	public float weight;
	
	public MCVertex(Vector3f pos){
		this.pos = pos;
		weight = DEFAULT_WEIGHT;
	}
}
