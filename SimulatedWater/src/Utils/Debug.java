package Utils;

/**
 * Debug class used to print debug messages. 
 * Has levels which can be used to determine what debug messages that will be printed
 *
 */
public class Debug {
	public static int MAX_DEBUG = 2;
	public static int MEDIUM_DEBUG = 1;
	public static int NO_DEBUG = 0;
	public static int mCounter = 0;
	
	
	private static int debugMode = MAX_DEBUG;
	
	public static void println(Object o,int level){
		if(debugMode >= level){
			System.out.println(o);
		}
	}
	
	public static void print(Object o,int level){
		if(debugMode >= level){
			System.out.print(o);
		}
	}
	
	public static void println(Object o){
		if(debugMode >= MEDIUM_DEBUG){
			System.out.println(o);
		}
	}
	
	public static void err(Object o,int level){
		if(debugMode >= level){
			System.err.println(o);
		}
	}
	
	public static void setDebugMode(int mode){
		debugMode = mode;
	}
	public static void counter(){
		System.out.println("Counter: " + mCounter);
		mCounter++;
	}
	
	public static void restartCounter(){
		mCounter = 0;
	}
}
