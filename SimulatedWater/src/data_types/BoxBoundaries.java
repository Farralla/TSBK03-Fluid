package data_types;

import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import Utils.MathUtils;

public class BoxBoundaries extends Boundaries {
	private static float EPSILON = 0.00001f;
	private float mSideX, mSideY, mSideZ;
	private ArrayList<Vector3f> mCorners;
	private ArrayList<Vector3f> mBaseVectors;

	public BoxBoundaries(Vector3f position, float side) {
		super(position);
		mSideX = side;
		mSideY = side;
		mSideZ = side;
		mCorners = new ArrayList<Vector3f>();
		Vector3f pos;

		// Add all corners
		// Bottom corners
		pos = Vector3f.add(mPosition, new Vector3f(-side / 2, -side / 2, side / 2), null);
		mCorners.add(pos);
		pos = Vector3f.add(mPosition, new Vector3f(-side / 2, -side / 2, -side / 2), null);
		mCorners.add(pos);
		pos = Vector3f.add(mPosition, new Vector3f(side, -side / 2, -side), null);
		mCorners.add(pos);
		pos = Vector3f.add(mPosition, new Vector3f(side, -side / 2, side / 2), null);
		mCorners.add(pos);

		// Top corners
		pos = Vector3f.add(mPosition, new Vector3f(-side / 2, side / 2, side / 2), null);
		mCorners.add(pos);
		pos = Vector3f.add(mPosition, new Vector3f(-side / 2, side / 2, -side / 2), null);
		mCorners.add(pos);
		pos = Vector3f.add(mPosition, new Vector3f(side, side / 2, -side), null);
		mCorners.add(pos);
		pos = Vector3f.add(mPosition, new Vector3f(side, side / 2, side / 2), null);
		mCorners.add(pos);

		// base vectors
		Vector3f bVec = new Vector3f(1, 0, 0);
		mBaseVectors = new ArrayList<Vector3f>();
		mBaseVectors.add(bVec);
		mNormals.add(bVec);
		mNormals.add((Vector3f) bVec.scale(-1));

		bVec = new Vector3f(0, 1, 0);
		mBaseVectors.add(bVec);
		mNormals.add(bVec);
		mNormals.add((Vector3f) bVec.scale(-1));

		bVec = new Vector3f(0, 0, 1);
		mBaseVectors.add(bVec);
		mNormals.add(bVec);
		mNormals.add((Vector3f) bVec.scale(-1));
	}

	@Override
	public void Rotate(Vector3f axis, float angle) {
		Matrix4f rotMat = new Matrix4f();
		rotMat.setIdentity();
		rotMat.rotate(angle, axis);
		rotMat.translate((Vector3f) mPosition.scale(-1f));
		for (Vector3f corner : mCorners) {
			corner = MathUtils.multMat4Vec3(rotMat, corner);
			corner = Vector3f.add(corner, mPosition, null);
		}
		// base vectors
		Vector3f bVec = Vector3f.sub(mCorners.get(3), mCorners.get(0), null);
		mBaseVectors.set(0, bVec);

		bVec = Vector3f.sub(mCorners.get(4), mCorners.get(0), null);
		mBaseVectors.set(1, bVec);

		bVec = Vector3f.sub(mCorners.get(1), mCorners.get(0), null);
		mBaseVectors.set(2, bVec);

		mNormals.set(0, mBaseVectors.get(0));
		mNormals.set(1, (Vector3f) mBaseVectors.get(0).scale(-1f));

		mNormals.set(2, mBaseVectors.get(1));
		mNormals.set(3, (Vector3f) mBaseVectors.get(1).scale(-1f));

		mNormals.set(4, mBaseVectors.get(2));
		mNormals.set(5, (Vector3f) mBaseVectors.get(2).scale(-1f));
		
		for(Vector3f n : mNormals){
			n.normalise();
		}
	}

	/**
	 * Projection into bounding planes with help of normals
	 * 
	 * @param particle
	 */
	@Override
	public void checkCollisions(Particle particle) {
		Vector3f p;
		Vector3f n;
		Vector3f r;
		float d = 0;
		for (int i = 0; i < 6; i++) {
			p = getACorner(i);
			n = mNormals.get(i);
			r = Vector3f.sub(particle.getPosition(), p, null);
			d = Vector3f.dot(n,r);
			
			if(d <= Particle.getH()){
				//COLLISION!
				if(d < 0)
					while(d<0){
						r = Vector3f.sub(particle.getPosition(), p, null);
						d = Vector3f.dot(n,r);
						particle.setPosition(Vector3f.add(
								particle.getPosition(),
								(Vector3f) n.scale(-EPSILON) , 
								null));
					}
					
				Vector3f rvec = (Vector3f) n.scale(-d);
				particle.handleCollision(rvec,Particle.getMass(),mDensity);
			}
		}
	}

	private ArrayList<Vector3f> getThreeCorners(int i) {
		ArrayList<Vector3f> corners = new ArrayList<Vector3f>();
		switch (i) {
		case (0):
			corners.add(mCorners.get(0));
			corners.add(mCorners.get(1));
			corners.add(mCorners.get(4));
			break;
		case (1):
			corners.add(mCorners.get(3));
			corners.add(mCorners.get(2));
			corners.add(mCorners.get(7));
			break;
		case (2):
			corners.add(mCorners.get(0));
			corners.add(mCorners.get(3));
			corners.add(mCorners.get(1));
			break;

		case (3):
			corners.add(mCorners.get(4));
			corners.add(mCorners.get(7));
			corners.add(mCorners.get(5));
			break;

		case (4):
			corners.add(mCorners.get(0));
			corners.add(mCorners.get(3));
			corners.add(mCorners.get(4));
			break;
		case (5):
			corners.add(mCorners.get(1));
			corners.add(mCorners.get(2));
			corners.add(mCorners.get(5));
			break;
		}
		return corners;
	}
	
	private Vector3f getACorner(int i) {
		switch (i) {
		case (0):
			return mCorners.get(0);
		case (1):
			return mCorners.get(3);
		case (2):
			return mCorners.get(0);
		case (3):
			return mCorners.get(4);
		case (4):
			return mCorners.get(0);
		case (5):
			return mCorners.get(1);
		}
		return mPosition;
	}

	public float getXHigh() {
		return mCorners.get(6).x;
	}

	public float getYHigh() {
		return mCorners.get(6).y;
	}

	public float getZHigh() {
		return mCorners.get(6).z;
	}
	
	public float getXLow() {
		return mCorners.get(0).x;
	}

	public float getYLow() {
		return mCorners.get(0).y;
	}

	public float getZLow() {
		return mCorners.get(0).z;
	}

	@Override
	public void checkCollisions(CollidableSphere c) {
		// TODO Auto-generated method stub
		
	}

}
