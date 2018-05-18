package edu.cg.scene.objects;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Sphere extends Shape {
	private Point center;
	private double radius;
	
	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + 
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}
	
	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}
	
	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}

	/**
	 * Intersect ray and sphere
	 * based on https://www.cs.princeton.edu/courses/archive/fall00/cs426/lectures/raycast/sld013.htm
	 * @param ray
	 * @return
	 */
	@Override
	public Hit intersect(Ray ray) { // TODO: make sure it works
		Vec l = center.sub(ray.source());
		double tca = l.dot(ray.direction());
		if(tca < 0) {
			// ray goes to opposite direction
			return null;
		}

		double dSquare = l.dot(l) - (tca * tca);
		double radSquare = radius * radius;
		if(dSquare > radSquare) {
			// no intersection
			return null;
		}
		double thc = Math.sqrt(radSquare - dSquare);
		double t = tca - thc;
		Vec normalToSphere = ray.add(t).sub(this.center).normalize();
		return new Hit(t, normalToSphere);
	}
}
