package edu.cg.scene.objects;

import edu.cg.algebra.*;

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
	 * based on https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection
	 * @param ray
	 * @return
	 */
	@Override
	public Hit intersect(Ray ray) {
        double a = 1;
        double b = 2 * (ray.direction().dot(ray.source().sub(center)));
        double c = Math.pow(ray.source().sub(center).length(), 2) - radius * radius;
        double disc = b * b - 4.0 * a * c;

        if(disc < Ops.epsilon || disc > Ops.infinity) {
            return null;
        }
        disc = Math.sqrt(disc);

        double t0 = (- b - disc) / (2 * a);
        double t1 = (- b + disc) / (2 * a);

        // If 2 intersections with sphere
        if(t0 < Ops.infinity && t0 > Ops.epsilon && t1 > Ops.epsilon) {
            Vec normalToSphere = ray.add(t0).sub(this.center).normalize();
            return new Hit(t0, normalToSphere).setIsWithin(false);
        }

        // One intersection
        if(t1 < Ops.infinity && t1 > Ops.epsilon) {
            Vec normalToSphere = ray.add(t1).sub(this.center).normalize().neg();
            return new Hit(t1, normalToSphere).setIsWithin(true);
        }
        return null;
	}

    // According to https://www.cs.princeton.edu/courses/archive/fall00/cs426/lectures/raycast/sld013.htm
    public double subsForP(Point p) {
        return p.distSqr(this.center) - Math.pow(this.radius, 2);
    }
}
