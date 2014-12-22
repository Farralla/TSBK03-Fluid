package rendering;

import static Utils.MathUtils.coTangent;
import static Utils.MathUtils.degreesToRadians;
import static Utils.MathUtils.setMatrix4f;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Utils.MathUtils;

public class Camera{
	private Matrix4f mViewMatrix;
	private Matrix4f mProjectionMatrix;
	private Vector3f mLookDir;
	private Vector3f mRightDir;
	private Vector3f mLookAt;
	private Vector3f mUpVec;
	private Vector3f mPos;
	
	public Camera(){
		mViewMatrix = new Matrix4f();
		mProjectionMatrix = new Matrix4f();
		mRightDir = new Vector3f();
		mLookDir = new Vector3f();
		mLookAt = new Vector3f();
		mUpVec = new Vector3f();
		mPos = new Vector3f();
	}
	
	
	/**
	 * Initializes camera with given initial states
	 * @param initialPos
	 * @param initialLookAt
	 * @param upVec
	 * @param fieldOfView
	 * @param aspectRatio
	 * @param nearPlane
	 * @param farPlane
	 */
	public void init(Vector3f initialPos, Vector3f initialLookAt, Vector3f upVec,float fieldOfView, float aspectRatio, float nearPlane, float farPlane){
		mPos.set(initialPos);
		mLookAt.set(initialLookAt);
		mUpVec.set(upVec);
		mLookDir = Vector3f.sub(mLookAt,mPos,null);
		mLookDir.normalise();
		mRightDir = Vector3f.cross(mLookDir,mUpVec,null);

		mViewMatrix = lookAt(mPos,mLookAt,mUpVec);
		mProjectionMatrix = calcProjectionMatrix(fieldOfView,aspectRatio,nearPlane,farPlane);
	}
	
	public void update(){
		float wheel = Mouse.getDWheel();
		if(Mouse.isButtonDown(0)){
			mouseRotate();
		}
		if(wheel != 0){
			mouseZoom(wheel);
		}
	}
	
	/**
	 * Rotate camera position around lookAtPoint as center
	 */
	private void mouseRotate(){
		float rotX = Mouse.getDX();
		float rotY = Mouse.getDY();
		
		Matrix4f rotMatX = MathUtils.rotatePointArroundCenter(
				mPos,
				mLookAt,
				mUpVec,
				(float)(rotX*0.01));
		
		Matrix4f rotMatY = MathUtils.rotatePointArroundCenter(
				mPos,
				mLookAt,
				mRightDir,
				(float)(rotY*0.01));
		
		Matrix4f rotTot = MathUtils.multMat4(rotMatX, rotMatY);
		mPos = MathUtils.multMat4Vec3(rotTot, mPos);
		
		mLookDir = Vector3f.sub(mLookAt,mPos,null);
		mLookDir.normalise();
		
		mRightDir = Vector3f.cross(mLookDir,mUpVec,null);
		mRightDir.normalise();
		
		mUpVec = Vector3f.cross(mRightDir,mLookDir,null);
		mUpVec.normalise();
		//Update viewMatrix
		mViewMatrix = lookAt(mPos,mLookAt,mUpVec);
		
		//System.out.println("camPos: "+ mPos);
		//System.out.println("camPos: "+ mPos);
	}
	
	/**
	 * Zoom in look direction
	 * @param wheel
	 */
	private void mouseZoom(float wheel){
		float zoomStep = wheel*0.001f;
		mPos = Vector3f.add(
				mPos, 
				MathUtils.scalarMultVec3(mLookDir, zoomStep), 
				null);
		
		mLookDir = Vector3f.sub(mLookAt,mPos,null);
		mLookDir.normalise();
		mRightDir = Vector3f.cross(mLookDir,mUpVec,null);
		
		//Update viewMatrix
		mViewMatrix = lookAt(mPos,mLookAt,mUpVec);
	}
	
	/**
	 * Computes 4x4 right hand view matrix
	 * @param pos Position of camera
	 * @param lookAt Camera look at point
	 * @param up Up vector often[0 1 0]
	 * @return 4x4 view matrix
	 */
	public static Matrix4f lookAt(Vector3f pos, Vector3f lookAt, Vector3f up){
		Vector3f n = new Vector3f();
		Vector3f u = new Vector3f();
		Vector3f v = new Vector3f();
		
		Vector3f.sub(pos, lookAt,n);
		n.normalise();
		
		Vector3f.cross(up, n, u);
		u.normalise();
		
	    Vector3f.cross(n, u, v);
	    v.normalise();

	    Matrix4f result = new Matrix4f();
	    result.m00 = u.x;
	    result.m10 = u.y;
	    result.m20 = u.z;
	    result.m01 = v.x;
	    result.m11 = v.y;
	    result.m21 = v.z;
	    result.m02 = n.x;
	    result.m12 = n.y;
	    result.m22 = n.z;
	    result.m33 = 1;
	    
	    Matrix4f trans = MathUtils.transMatrix(-pos.x,-pos.y,-pos.z);
	    result = MathUtils.multMat4(result,trans);
	    //result = Matrix4f.translate(new Vector3f(-pos.x, -pos.y, -pos.z),result,null);
	    
	    //I should not need to transpose.... but it works
		//Matrix4f.transpose(result, result);
	    return result;
	}
	
	/**
	 * Calculates projection matrix from the frustum settings
	 * @param fov
	 * @param aspectRatio
	 * @param nearPlane
	 * @param farPlane
	 * @return
	 */
	public static Matrix4f calcProjectionMatrix(float fov, float aspectRatio, float nearPlane, float farPlane){
		Matrix4f projectionMatrix = new Matrix4f();

		float y_scale = coTangent(degreesToRadians(fov/ 2f));
		float x_scale = y_scale / aspectRatio;
		float frustumLength = farPlane - nearPlane;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((farPlane + nearPlane) / frustumLength);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * nearPlane * farPlane) / frustumLength);
		projectionMatrix.m33 = 0;
		
		//Matrix4f.transpose(projectionMatrix, projectionMatrix);
		return projectionMatrix;
	}
	
	/**
	 * 
	 * @param fov
	 * @param aspectRatio
	 * @param znear
	 * @param zfar
	 * @return
	 */
	Matrix4f perspective(float fov, float aspectRatio, float znear, float zfar){
		float ymax,xmax;
		ymax = (float) (znear * Math.tan(fov*Math.PI/360.0));
		
		if (aspectRatio < 1.0)
	    {
		    ymax = (float) (znear * Math.tan(fov * Math.PI / 360.0));
	       xmax = ymax * aspectRatio;
	    }
	    else
	    {
		    xmax = (float) (znear * Math.tan(fov * Math.PI / 360.0));
	       ymax = xmax / aspectRatio;
	    }
		
		return frustum(-xmax, xmax, -ymax, ymax, znear, zfar);
	}
	
	Matrix4f frustum(float left, float right, float bottom,float top, float znear, float zfar){
		float temp,temp2,temp3,temp4;
		Matrix4f matrix = new Matrix4f();
		
		temp = 2*znear;
		temp2 = right-left;
		temp3 = top-bottom;
		temp4 = zfar-znear;
		
		matrix = setMatrix4f(
				temp/temp2, 0f, 0f, 0f,
				0f, temp/temp3, 0f, 0f,
				(right+left)/temp2, (top+bottom)/temp3, (-zfar-znear)/temp4, -1f,
				0f,0f,(-temp*zfar)/temp4, 0f);
		//Matrix4f.transpose(matrix, matrix);
		return matrix;
	}
	
	public Matrix4f getViewMatrix(){
		return mViewMatrix;
	}
	
	public Matrix4f getProjectionMatrix(){
		return mProjectionMatrix;
	}
	
	public Vector3f getLookAt(){
		return mLookAt;
	}
	
	public Vector3f getPos(){
		return mPos;
	}
	
	public Vector3f getUpVec(){
		return mUpVec;
	}
	
	public void setViewMatrix(Matrix4f viewMatrix){
		mViewMatrix = viewMatrix;
	}
	
	public void setProjectionMatrix(Matrix4f projectionMatrix){
		mProjectionMatrix = projectionMatrix;
	}
}
