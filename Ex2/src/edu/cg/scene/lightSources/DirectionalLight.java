package edu.cg.scene.lightSources;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class DirectionalLight extends Light {
	private Vec direction = new Vec(0, -1, -1);

	public DirectionalLight initDirection(Vec direction) {
		this.direction = direction;
		return this;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Directional Light:" + endl + super.toString() +
				"Direction: " + direction + endl;
	}

	@Override
	public DirectionalLight initIntensity(Vec intensity) {
		return (DirectionalLight)super.initIntensity(intensity);
	}

	@Override
	public double calculateCosAngleBetweenNormalAndLight(Vec normalToSurface, Point point) {
		return direction.neg().normalize().dot(normalToSurface);
	}

	@Override
	public Vec intensityForPoint(Point point) {
		return intensity;
	}

	//TODO: add some methods
}
