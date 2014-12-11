package Utils;

public class Timer {
	private double timeStamp;
	private double dT;
	private boolean on = true;
	
	public Timer(){
		timeStamp = timeStamp = System.currentTimeMillis(); 
	}
	public void update(){
		if(!on) 
			return;
		dT = 0.001*(System.currentTimeMillis() - timeStamp);
		timeStamp = System.currentTimeMillis();
	}
	
	public void init(){
		if(!on)
			return;
		timeStamp = System.currentTimeMillis();
	}
	
	public  void println(){
		if(!on)
			return;
		Debug.println("Timer dT: " + dT, Debug.MAX_DEBUG);
	}
	
	public void println(String s){
		if(!on)
			return;
		Debug.println("Timer dT: " + s + dT, Debug.MAX_DEBUG);
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
