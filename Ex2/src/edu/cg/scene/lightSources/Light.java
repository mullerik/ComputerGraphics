package edu.cg.scene.lightSources;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public abstract class Light {
	protected Vec intensity = new Vec(1, 1, 1); //white color
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Intensity: " + intensity + endl;
	}
	
	public Light initIntensity(Vec intensity) {
		this.intensity = intensity;
		return this;
	}

	public abstract double calculateCosAngleBetweenNormalAndLight(Vec normalToSurface, Point point);

	public abstract Vec intensityForPoint(Point point);


	//TODO: add some methods
}
