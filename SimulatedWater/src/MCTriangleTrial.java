import marching_cubes.MCTriangle;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL20.glUseProgram;

import Rendering.Renderer;
import Utils.GLUtils;
import data_types.Liquid;

public class MCTriangleTrial {
		public static final int WIDTH = 640;
		public static final int HEIGHT = 480;
		
	public static void main(String[] args) throws Exception {
		
		//Initiate renderer
		Renderer renderingUnit= new Renderer(WIDTH,HEIGHT);
		renderingUnit.init();
		
		MCTriangle t = new MCTriangle(new Vector3f(0f,0f,0f),new Vector3f(1f,1f,1f), new Vector3f(0f, 1f, 0f));
		
		//Initiate Loop
		//int updateCount = 0;
		while (!Display.isCloseRequested()) {
			//updateCount++;
			renderingUnit.drawMCTriangle(t);
			//System.out.println(updateCount);
			
			// Force a maximum FPS of about 30 ----> dT = 1/30 = 0.033
			Display.sync(30);
			// Let the CPU synchronize with the GPU if GPU is tagging behind
			Display.update();
		}
	}
}
