package Utils;

import static java.lang.Math.pow;

import org.lwjgl.util.vector.Vector3f;

public class Kernel {
	private float h;
	private float hSq;
	
	
	//Constants
	private float poly6Constant;
	private float poly6GradConstant;
	private float poly6LapConstant;
	private float gaussConstant;
	private float pressureGradConstant;
	private float viscosityLapConstant;
	
	public void updateConstants(float h){
		poly6Constant = (float) (315/(64*Math.PI*pow(h,9)));
		poly6GradConstant = (float) -(945/(32*Math.PI*pow(h,9)));
		poly6LapConstant = (float) -(945/(32*Math.PI*pow(h,9)));
		gaussConstant = (float) (1/(Math.pow(Math.PI, 3/2)*pow(h,3)));
		pressureGradConstant = (float) -(45/(Math.PI*pow(h,6)));
		viscosityLapConstant = (float) (45/(Math.PI*pow(h,6)));
	}
	
	public Kernel(float h){
		this.h = h;
		this.hSq = (float) Math.pow(h,2);
		updateConstants(h);
	}
	
	/**
	 * General weight function
	 * Used for density weight
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public float W_poly6(Vector3f r_vec){
		float weight = 0;
		float r = r_vec.length();
		//weight = (float) (315/(64*Math.PI*pow(h,9))*(pow((pow(h,2)-pow(r,2)),3)));
		
		weight = (float) (poly6Constant*(pow(hSq-pow(r,2),3)));
		return weight;
	}
	
	/**
	 * Gradient of poly6(general) kernel
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public Vector3f GradW_poly6(Vector3f r_vec) {
		Vector3f gradW = new Vector3f(r_vec);
		float r = r_vec.length();
		float W = 0;
		//W = (float) -(945/(32*Math.PI*pow(h,9))*Math.pow((pow(h,2)-pow(r,2)),2));
		W = (float) (poly6GradConstant*pow((hSq-pow(r,2)),2));
		
		gradW.scale(W);
		return gradW;
	}
	
	/**
	 * Laplacian of poly6(general) kernel
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public float LapW_poly6(Vector3f r_vec) {
		float r = r_vec.length();
		float W = 0;
		//W = (float) ((945/(8*Math.PI*pow(h,9))*(temp))*(pow(r,2)-3/4*(temp)));
//		W = (float) (poly6LapConstant*temp*(pow(r,2)-3*temp/4));
		W = (float) (poly6LapConstant * (pow(h,2)-pow(r,2)) * (3*pow(h,2)-7*pow(r, 2)));
		return W;
	}
	
	/**
	 * General weight function of gaussian type
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public float W_gauss(Vector3f r_vec){
		float weight = 0;
		float r = r_vec.length();
		weight = (float) (gaussConstant*Math.exp(pow(r,2)/hSq));
		return weight;
	}
	
	/**
	 * Gradient of pressure(spiky) kernel
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public Vector3f GradW_pressure(Vector3f r_vec) {
		Vector3f gradW = new Vector3f(r_vec);
		float r = r_vec.length();
		if(r<0.00000001f){
			r=0.00000001f;
		}
		float W = 0;
		//W = (float) -(45/(Math.PI*pow(h,6)*r)*Math.pow((h-r),2));
		W = (float)  (pressureGradConstant / r * pow((h-r),2));
		gradW.scale(W);
		return gradW;
	}
	
	/**
	 * Laplacian of viscosity kernel function
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public float LapW_viscosity(Vector3f r_vec){
		float r = r_vec.length();
		float weight = 0;
		//weight = (float) (45/(Math.PI*pow(h,6))*(h-r));
		weight = (float) viscosityLapConstant*(h-r);
		return weight;
	}
}
