package data_types;

import org.lwjgl.util.vector.Vector3f;

public interface ParticleInterface {
	public float getDensity();
	public float getPressure();
	public Vector3f getPosition();
}
