import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import Rendering.Renderer;
import Utils.GLUtils;
import data_types.Liquid;

public class Simulation {
		public static final int WIDTH = 640;
		public static final int HEIGHT = 480;
		
	public static void main(String[] args) throws Exception {
		
		//Initiate Liquid
		Liquid liquid = new Liquid(700,0.05f,0.005f,0.1f);
		liquid.init();
		
		//Initiate renderer
		Renderer renderingUnit= new Renderer(WIDTH,HEIGHT);
		renderingUnit.init();
		
		Vector3f[] vArray = {new Vector3f(0,0,0), new Vector3f(1,1,1)};
		float[] array = GLUtils.toArray(vArray);
		
		
		//Initiate Loop
		//int updateCount = 0;
		while (!Display.isCloseRequested()) {
			//updateCount++;
			//System.out.println(updateCount);
			
			//Update liquid
			liquid.update();
			
			//Draw liquid
			renderingUnit.drawLiquid(liquid);
			
			// Force a maximum FPS of about 30 ----> dT = 1/30 = 0.033
			Display.sync(30);
			// Let the CPU synchronize with the GPU if GPU is tagging behind
			Display.update();
		}
	}
}
