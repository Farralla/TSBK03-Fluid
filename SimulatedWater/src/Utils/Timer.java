package Utils;

public class Timer {
	private double timeStamp;
	private double dT;
	private boolean on = true;
	private double type;
	
	public static final double SEC = 0.000001f;
	public static final double MILLIS = 0.001f;
	public static final double MICROS = 1f;
	
	
	public Timer(){
		timeStamp = System.nanoTime()/1000; 
		type = MILLIS;
	}
	
	public Timer(double type){
		timeStamp = System.nanoTime()/1000; 
		this.type = type;
	}
	
	public void update(){
		if(!on) 
			return;
		dT = type*(System.nanoTime()/1000 - timeStamp);
		timeStamp = System.nanoTime()/1000;
	}
	
	public void setType(double type){
		this.type = type;
	}
	
	public void init(){
		if(!on)
			return;
		timeStamp = System.currentTimeMillis();
	}
	
	public  void println(){
		if(!on)
			return;
		Debug.println("Timer dT: " + (float)dT, Debug.MAX_DEBUG);
	}
	
	public void println(String s){
		if(!on)
			return;
		Debug.println("Timer dT: " + s + " " + (float)dT, Debug.MAX_DEBUG);
	}
	
	public double getTimeStamp(){
		return timeStamp;
	}
	
	public double getdT(){
		return dT;
	}
	
	public void off(){
		on = false;
	}
	
	public void on(){
		on = true;
	}
}
