package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Dome extends Shape {
	private Sphere sphere;
	private Plain plain;
	
	public Dome() {
		sphere = new Sphere().initCenter(new Point(0, -0.5, -6));
		plain = new Plain(new Vec(-1, 0, -1), new Point(0, -0.5, -6));
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Dome:" + endl + 
				sphere + plain + endl;
	}

	/**
	 * Intersect ray with dome by splitting to cases.
	 * @param ray
	 * @return
	 */
	@Override
	public Hit intersect(Ray ray) { // TODO: make sure it works TODO: do we need logic for case that camera is inside the dome?
		Hit sphereIntersection = sphere.intersect(ray);
		Hit plainIntersection = plain.intersect(ray);

		// If ray does not intersect sphere, no intersection at all
		if(sphereIntersection == null) {
			return null;
		}

		Point sphereHitPoint = ray.add(sphereIntersection.t());

		// Check if sphere intersection is above the plain:
		if(plain.isAbovePlain(sphereHitPoint)) {
			return sphereIntersection;
		}

		// If does not intersect the plain at all
		if(plainIntersection == null) {
			return null;
		}

		// Check if the plainIntersection is inside the sphere
		Point planeHitPoint = ray.add(plainIntersection.t());
		if(sphere.isPointInsideSphere(planeHitPoint)) {
			return plainIntersection;
		}
		return null;
	}
}
