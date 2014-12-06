package marching_cubes;

import java.util.HashMap;

import org.lwjgl.util.vector.Vector3f;

import data_types.ParticleInterface;

public interface CubeInterface {
	public Vector3f getPosition();
	public float getSize();
	public boolean containsParticle(ParticleInterface particle);
	public HashMap<Integer, MCCube> getNeighbours();
	public void addParticle(ParticleInterface particle);
	public void removeParticle(ParticleInterface particle);
}
