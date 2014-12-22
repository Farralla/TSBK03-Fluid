package data_types;

import org.lwjgl.util.vector.Vector3f;

import data_types.Liquid.Boundaries;

public class CollidableSphere extends Collidable {
	private static final float EPSILON = 0.00001f;
	private Liquid mLiquid;

	public CollidableSphere(Liquid liquid, Vector3f position, float radius) {
		super(position, radius);
		mLiquid = liquid;
	}

	public CollidableSphere(Liquid liquid,Vector3f position, float radius, float mass, float density) {
		super(position, radius,mass,density);
		mLiquid = liquid;
	}

	@Override
	void update() {
		Vector3f acceleration = new Vector3f(0, 0, 0);
		
		acceleration = Vector3f.add(
				(Vector3f) mForce.scale((float) (Liquid.dT / mDensity)),
				(Vector3f) Liquid.gravity().scale(1f),
				null);

		mVelocity = Vector3f.add(mVelocity,
				(Vector3f) acceleration.scale((float) Liquid.dT),
				null);
		
		//Cheating with water friction
		mVelocity.scale(0.97f);
		
		Vector3f v = new Vector3f(mVelocity);
		synchronized (this) {
			mPosition = Vector3f.add(mPosition, (Vector3f) v.scale((float) Liquid.dT), null);
		}
		
		mForce.set(0, 0, 0);
		
		checkBoundaries(mLiquid);
	}
	
	private void checkBoundaries(Liquid liquid) {
		Boundaries b = liquid.getBoundaries();
		//float k = 0.6f;
		if (b.isSideConstraintsOn())
		{
			if (mPosition.x < b.xLow+mRadius) {
				mPosition.x = b.xLow+mRadius + EPSILON;
				mVelocity.x = 0;
			}
			else if (mPosition.x > b.xHigh-mRadius) {
				mPosition.x = b.xHigh-mRadius - EPSILON;
				mVelocity.x = 0;
			}

			if (mPosition.z < b.zLow+mRadius) {
				mPosition.z = b.zLow+mRadius+EPSILON;
				mVelocity.z = 0;
			}
			else if (mPosition.z > b.zHigh-mRadius) {
				mPosition.z = b.zHigh-mRadius-EPSILON;
				mVelocity.z = 0;
			}
		}

		if (mPosition.y < b.yLow+mRadius) {
			mPosition.y = b.yLow+mRadius+EPSILON;
			mVelocity.y = 0;
		}
		else if (mPosition.y > b.yHigh-mRadius) {
			mPosition.y = b.yHigh-mRadius-EPSILON;
			mVelocity.y = 0;
		}
	}

}
