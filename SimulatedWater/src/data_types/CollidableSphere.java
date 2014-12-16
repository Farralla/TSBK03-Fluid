package data_types;

import org.lwjgl.util.vector.Vector3f;

import Utils.Debug;
import data_types.Liquid.Boundaries;

public class CollidableSphere extends Collidable {
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
				(Vector3f) Liquid.gravity().scale(0.1f),
				null);

		mVelocity = Vector3f.add(mVelocity,
				(Vector3f) acceleration.scale((float) Liquid.dT),
				null);
		
		// float k = 1 - mVelocity.length() * 0.01f;
		// mVelocity.scale(k);

		// if (mVelocity.length() > MAX_VELOCITY) {
		// mVelocity.normalise().scale(MAX_VELOCITY);
		// }

		synchronized (this) {
			mPosition = Vector3f.add(mPosition, (Vector3f) mVelocity.scale((float) Liquid.dT), null);
		}

		mForce.set(0, 0, 0);
		
		checkBoundaries(mLiquid);
	}
	
	private void checkBoundaries(Liquid liquid) {
		Boundaries b = liquid.getBoundaries();
		if (b.isSideConstraintsOn())
		{
			if (mPosition.x < b.xLow+mRadius) {
				mPosition.x = b.xLow+mRadius;
			}
			else if (mPosition.x > b.xHigh-mRadius) {
				mPosition.x = b.xHigh-mRadius;
			}

			if (mPosition.z < b.zLow+mRadius) {
				mPosition.z = b.zLow+mRadius;
			}
			else if (mPosition.z > b.zHigh-mRadius) {
				mPosition.z = b.zHigh-mRadius;
			}
		}

		if (mPosition.y < b.yLow+mRadius) {
			mPosition.y = b.yLow+mRadius;
		}
		else if (mPosition.y > b.yHigh-mRadius) {
			mPosition.y = b.yHigh-mRadius;
		}
	}

}
