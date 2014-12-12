package Utils;

public class Timer {
	private double timeStamp;
	private double dT;
	private boolean on = true;
	private float type;
	
	public static final float SEC = 0.001f;
	public static final float MILLIS = 1f;
	
	
	public Timer(){
		timeStamp = System.currentTimeMillis(); 
		type = SEC;
	}
	public void update(){
		if(!on) 
			return;
		dT = type*(System.currentTimeMillis() - timeStamp);
		timeStamp = System.currentTimeMillis();
	}
	
	public void setType(float type){
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
