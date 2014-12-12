import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import Rendering.LiquidRenderer;
import Rendering.Renderer;
import Utils.Debug;
import Utils.GLUtils;
import Utils.Timer;
import data_types.Liquid;

public class Simulation {
		public static final int WIDTH = 640;
		public static final int HEIGHT = 480;
		
		public static double timeStamp;
		public static double dT;
		
	public static void main(String[] args) throws Exception {
		Debug.setDebugMode(Debug.MAX_DEBUG);
		//Initiate Liquid
		Liquid liquid = new Liquid(1000,0.3f,0.03f,0.1f);
		liquid.init();
		
		//Initiate renderer
		LiquidRenderer renderingUnit= new LiquidRenderer(WIDTH,HEIGHT,liquid);
		Thread renderingThread = new Thread(renderingUnit);
		renderingThread.setDaemon(true);
		renderingThread.start();
		
		//Timer
		Timer timer = new Timer();
		timer.init();
		timer.off();
		
		//Initiate Loop
		//int updateCount = 0;
		while (true) {
			timer.update();
			//timer.println();
			
			//Update liquid
			liquid.update();
			timer.update();
			timer.println("Updated liquid");
		}
	}
}
