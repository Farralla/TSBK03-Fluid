package Utils;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public final class MathUtils {
	boolean  transposed = false;
	
	/**
	 * Set each element in a matrix, rowwise order
	 * @param m00
	 * @param m01
	 * @param m02
	 * @param m03
	 * @param m10
	 * @param m11
	 * @param m12
	 * @param m13
	 * @param m20
	 * @param m21
	 * @param m22
	 * @param m23
	 * @param m30
	 * @param m31
	 * @param m32
	 * @param m33
	 * @return
	 */
	public static Matrix4f setMatrix4f(
			float m00,float m01,float m02,float m03,
			float m10,float m11,float m12,float m13,
			float m20,float m21,float m22,float m23,
			float m30,float m31,float m32,float m33){
		Matrix4f returnMat = new Matrix4f();
		returnMat.m00 = m00;
		returnMat.m01 = m01;
		returnMat.m02 = m02;
		returnMat.m03 = m03;
		returnMat.m10 = m10;
		returnMat.m11 = m11;
		returnMat.m12 = m12;
		returnMat.m13 = m13;
		returnMat.m20 = m20;
		returnMat.m21 = m21;
		returnMat.m22 = m22;
		returnMat.m23 = m23;
		returnMat.m30 = m30;
		returnMat.m31 = m31;
		returnMat.m32 = m32;
		returnMat.m33 = m33;
		Matrix4f.transpose(returnMat, returnMat);
		return returnMat;
		
	}
	
	/**
	 * Convert degrees to radians
	 * @param degrees
	 * @return
	 */
	public static float degreesToRadians(float degrees){
		return degrees*(float)Math.PI/180;
	}
	
	/**
	 * coTangent
	 * @param angle
	 * @return
	 */
	public static float coTangent(float angle){
		return (float) (1/Math.tan(angle));
	}
	
	/**
	 * Creates translation matrix from vector with (x,y,z)-coordinates
	 * @param v
	 * @return
	 */
	public static Matrix4f transMatrix(Vector3f v){
		Matrix4f m = new Matrix4f();
		m.setIdentity();
		m.m30 = v.x;
		m.m31 = v.y;
		m.m32 = v.z;
		//Matrix4f.transpose(m, m);
		return m;
	}
	
	/**
	 * Creats translation matrix from (x,y,z)-coordinates
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static Matrix4f transMatrix(float x, float y, float z){
		Matrix4f m = new Matrix4f();
		m.setIdentity();
		m.m30 = x;
		m.m31 = y;
		m.m32 = z;
		//Matrix4f.transpose(m, m);
		return m;
	}
	/**
	 * Multiplication: Matrix4f by Matrix4f
	 * @param m1
	 * @param m2
	 * @return m1*m2
	 */
	public static Matrix4f multMat4(Matrix4f m1, Matrix4f m2){
		return Matrix4f.mul(m1, m2, null);	
	}
	
	/**
	 * Multiplication: Matrix4f by Vector4f
	 * @param m1
	 * @param v1
	 * @return
	 */
	public static Vector4f multMat4Vec4(Matrix4f m1, Vector4f v1){
		return Matrix4f.transform(m1, v1, null);
	}
	
	/**
	 * Multiplication of rotation part of Matrix4f by Vector3f
	 * @param m
	 * @param v
	 * @return
	 */
	public static Vector3f multMat4Vec3(Matrix4f a, Vector3f b){
		Vector3f r = new Vector3f();
		r.x = a.m00*b.x + a.m10*b.y + a.m20*b.z + a.m30;
		r.y = a.m01*b.x + a.m11*b.y + a.m21*b.z + a.m31;
		r.z = a.m02*b.x + a.m12*b.y + a.m22*b.z + a.m32;
		return r;
	}
	
	/**
	 * Convert Matrix4f to Matrix3f
	 * @param m4
	 * @return
	 */
	public static Matrix3f mat4ToMat3(Matrix4f m4){
		Matrix3f m3 = new Matrix3f();
		m3.m00 = m4.m00;
		m3.m10 = m4.m10;
		m3.m20 = m4.m20;
		
		m3.m01 = m4.m01;
		m3.m11 = m4.m11;
		m3.m21 = m4.m21;
		
		m3.m02 = m4.m02;
		m3.m12 = m4.m12;
		m3.m22 = m4.m22;
		
		return m3;
	}
	
	public static Vector4f vec3ToVec4(Vector3f v3){
		Vector4f v4 = new Vector4f();
		v4.x = v3.x;
		v4.y = v3.y;
		v4.z = v4.z;
		v4.w = 1;
		return v4;
	}
	
	public static Vector3f vec4ToVec3(Vector4f v4){
		Vector3f v3 = new Vector3f();
		v3.x = v4.x;
		v3.y = v4.y;
		v3.z = v4.z;
		return v3;
	}
	
	public static Matrix4f Rx(float a){
		Matrix4f m = new Matrix4f();
		m.setIdentity();
		//m.m[5] = Math.cos(a);
		m.m11 = (float) Math.cos(a);
		
		//if non transposed
		m.m12 = (float)Math.sin(a);
		
		m.m21 = -m.m12; //sin(a);
		m.m22 = m.m11; //cos(a);
		return m;
	}

	public static Matrix4f Ry(float a){
		Matrix4f m= new Matrix4f();
		m.setIdentity();
		m.m00 = (float) Math.cos(a);
		
		//if non transposed
		m.m02 = (float)Math.sin(a);
		
		m.m20 = -m.m02; //sin(a);
		m.m22 = m.m00; //cos(a);
		return m;
	}

	public static Matrix4f Rz(float a){
		Matrix4f m = new Matrix4f();
		m.setIdentity();
		m.m00 = (float) Math.cos(a);
		
		//if non transposed
		m.m01 = (float)Math.sin(a);
		
		m.m10 = -m.m01; //sin(a);
		m.m11 = m.m00; //cos(a);
		return m;
	}
	
	public static Matrix4f R(float angle, Vector3f axis){
		Matrix4f m = new Matrix4f();
		m.setIdentity();
		return m.rotate(angle, axis);
	}
	
	
        /**
         * R
         * @param point
         * @param rotAxis
         * @param angle
         * @return
         */
	public static Matrix4f rotatePointArroundCenter(Vector3f point, Vector3f center, Vector3f rotAxis, float angle){
		Matrix4f m = new Matrix4f();
		m.setIdentity();
		m.translate(new Vector3f(-point.x,-point.y,-point.z));
		m.translate(new Vector3f(-center.x,-center.y,-center.z));
		m.rotate(angle, rotAxis);
		m.translate(center);
		m.translate(point);
		return m;
	}
	
	public static Vector3f scalarMultVec3(Vector3f v,float f){
		Vector3f r = new Vector3f();
		r.x = v.x*f;
		r.y = v.y*f;
		r.z = v.z*f;
		return r;
	}
	
	/**
	 * Convert the input in to decimal count n
	 * @param in input float to decimate
	 * @param n number of decimals
	 * @return float with n number of decimals
	 */
	public static float toDecimals(float in,int n){
		float temp = (float) Math.pow(10, n);
		return (float) (Math.ceil(in*temp)/temp);
	}
	
}
