import Rendering.LiquidRenderer;
import Utils.Debug;
import Utils.Timer;
import data_types.Liquid;

public class Simulation {
		public static final int WIDTH = 640;
		public static final int HEIGHT = 480;
		
		
	public static void main(String[] args) throws Exception {
		Debug.setDebugMode(Debug.MAX_DEBUG);
		//Initiate Liquid
		Liquid liquid = new Liquid(500,0.4f,0.02f,200f);
		
		//Initiate renderer
		LiquidRenderer renderingUnit= new LiquidRenderer(WIDTH,HEIGHT,liquid);
		Thread renderingThread = new Thread(renderingUnit);
		renderingThread.setDaemon(true);
		renderingThread.start();
		
		//Timer
		Timer timer = new Timer();
		timer.init();
		timer.on();

		
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
