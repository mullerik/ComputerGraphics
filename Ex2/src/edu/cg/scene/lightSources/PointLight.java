package edu.cg.scene.lightSources;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class PointLight extends Light {
	protected Point position;
	
	//Decay factors:
	protected double kq = 0.01;
	protected double kl = 0.1;
	protected double kc = 1;
	
	protected String description() {
		String endl = System.lineSeparator();
		return "Intensity: " + intensity + endl +
				"Position: " + position + endl +
				"Decay factors: kq = " + kq + ", kl = " + kl + ", kc = " + kc + endl;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Point Light:" + endl + description();
	}
	
	@Override
	public PointLight initIntensity(Vec intensity) {
		return (PointLight)super.initIntensity(intensity);
	}

	@Override
	public double calculateCosAngleBetweenNormalAndLight(Vec normalToSurface, Point point) {
		return position.sub(point).normalize().dot(normalToSurface);
	}

	@Override
	public Vec intensityForPoint(Point point) {
	    double d = position.sub(point).length();
	    double attenuationFactor = 1 / (kc + kl*d + kq*d*d);
		return intensity.mult(attenuationFactor);
	}

    @Override
    protected Vec vectorFromPointToLight(Point point) {
        return position.sub(point);
    }

    public PointLight initPosition(Point position) {
		this.position = position;
		return this;
	}
	
	public PointLight initDecayFactors(double kq, double kl, double kc) {
		this.kq = kq;
		this.kl = kl;
		this.kc = kc;
		return this;
	}

    @Override
    public double distanceToLight(Point point) {
        return point.sub(position).length();
    }
}
