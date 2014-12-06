package Utils;

public class Debug {
	public static int MAX_DEBUG = 2;
	public static int MEDIUM_DEBUG = 1;
	public static int NO_DEBUG = 0;
	
	
	private static int debugMode = MAX_DEBUG;
	
	public static void println(Object o){
		if(debugMode == MEDIUM_DEBUG){
			System.out.println(o);
		}
	}
	
	public static void println(Object o,int level){
		if(debugMode >= level){
			System.out.println(o);
		}
	}
	
	public static void setDebugLevel(){
		debugMode = MAX_DEBUG;
	}
	
	public static void err(Object o,int level){
		if(debugMode >= level){
			System.out.println(o);
		}
	}
}
