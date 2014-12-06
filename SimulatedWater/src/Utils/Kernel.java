package Utils;

import static java.lang.Math.pow;

import org.lwjgl.util.vector.Vector3f;

public class Kernel {
	/**
	 * General weight function
	 * Used for density weight
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public static float W_poly6(Vector3f r_vec,float h){
		float weight = 0;
		float r = r_vec.length();
		//System.out.println("r : " + r);
		weight = (float) (315/(64*Math.PI*pow(h,9))*(pow((pow(h,2)-pow(r,2)),3)));
		return weight;
	}
	
	/**
	 * Gradient of poly6(general) kernel
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public static Vector3f GradW_poly6(Vector3f r_vec, float h) {
		Vector3f gradW = new Vector3f(r_vec);
		float r = r_vec.length();
		float W = 0;
		W = (float) -(945/(32*Math.PI*pow(h,9))*Math.pow((pow(h,2)-pow(r,2)),2));
		gradW.scale(W);
		return gradW;
	}
	
	/**
	 * Laplacian of poly6(general) kernel
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public static float LapW_poly6(Vector3f r_vec, float h) {
		float r = r_vec.length();
		float W = 0;
		float temp = (float) (pow(h,2)-pow(r,2));
		W = (float) ((945/(8*Math.PI*pow(h,9))*(temp))*(pow(r,2)-3/4*(temp)));
		return W;
	}
	
	/**
	 * General weight function of gaussian type
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public static float W_gauss(Vector3f r_vec, float h){
		float weight = 0;
		float r = r_vec.length();
		weight = (float) (1/(Math.pow(Math.PI, 3/2)*pow(h,3))*Math.exp(pow(r,2)/pow(h,2)));
		return weight;
	}
	
	/**
	 * Gradient of pressure(spiky) kernel
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public static Vector3f GradW_pressure(Vector3f r_vec, float h) {
		Vector3f gradW = new Vector3f(r_vec);
		float r = r_vec.length();
		if(r==0){
			r=0.01f;
		}
		float W = 0;
		W = (float) -(45/(Math.PI*pow(h,6)*r)*Math.pow((h-r),2));
		gradW.scale(W);
		return gradW;
	}
	
	/**
	 * Laplacian of viscosity kernel function
	 * @param r_vec
	 * @param h
	 * @return
	 */
	public static float LapW_viscosity(Vector3f r_vec,float h){
		float r = r_vec.length();
		float weight = 0;
		weight = (float) (45/(Math.PI*pow(h,6))*(h-r));
		return weight;
	}
}
