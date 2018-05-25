package edu.cg.algebra;

import edu.cg.UnimplementedMethodException;

public class Ops {
	public static final double epsilon = 1e-5;
	public static final double infinity = 1e8;
	public static final Vec epsilonVec = new Vec(epsilon);

	public static double dot(Vec u, Vec v) {
		return u.x*v.x + u.y*v.y + u.z*v.z;
	}
	
	public static Vec cross(Vec u, Vec v) {
		return new Vec((u.y*v.z - u.z*v.y), (u.z*v.x - u.x*v.z), (u.x*v.y - u.y*v.x));
	}
	
	public static Vec mult(double a, Vec v) {
		return mult(new Vec(a), v);
	}
	
	public static Vec mult(Vec u, Vec v) {
		return new Vec(u.x*v.x, u.y*v.y, u.z*v.z);
	}
	
	public static Point mult(double a, Point p) {
		return mult(new Point(a), p);
	}
	
	public static Point mult(Point p1, Point p2) {
		return new Point(p1.x*p2.x, p1.y*p2.y, p1.z*p2.z);
	}
	
	public static double normSqr(Vec v) {
		return dot(v, v);
	}
	
	public static double norm(Vec v) {
		return Math.sqrt(normSqr(v));
	}
	
	public static double lengthSqr(Vec v) {
		return normSqr(v);
	}
	
	public static double length(Vec v) {
		return norm(v);
	}
	
	public static double dist(Point p1, Point p2) {
		return length(sub(p1, p2));
	}
	
	public static double distSqr(Point p1, Point p2) {
		return lengthSqr(sub(p1, p2));
	}
	
	public static Vec normalize(Vec v) {
		return mult(1.0/norm(v), v);
	}
	
	public static Vec neg(Vec v) {
		return mult(-1, v);
	}
	
	public static Vec add(Vec u, Vec v) {
		return new Vec(u.x+v.x, u.y+v.y, u.z+v.z);
	}
	
	public static Point add(Point p, Vec v) {
		return new Point(p.x+v.x, p.y+v.y, p.z+v.z);
	}
	
	public static Point add(Point p1, Point p2) {
		return new Point(p1.x+p2.x, p1.y+p2.y, p1.z+p2.z);
	}
	
	public static Point add(Point p, double t, Vec v) {
		//returns p + tv;
		return add(p, mult(t, v));
	}
	
	public static Vec sub(Point p1, Point p2) {
		return new Vec(p1.x-p2.x, p1.y-p2.y, p1.z-p2.z);
	}
	
	public static boolean isFinite(Vec v) {
		return Double.isFinite(v.x) & Double.isFinite(v.y) & Double.isFinite(v.z);
	}

	public static boolean isFinite(Point p) {
		return Double.isFinite(p.x) & Double.isFinite(p.y) & Double.isFinite(p.z);
	}

	/**
	 * Based on https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector
	 * @param u
	 * @param normal
	 * @return
	 */
	public static Vec reflect(Vec u, Vec normal) {
		return u.add(normal.mult(u.dot(normal) * -2));
	}
	
	public static Vec refract(Vec u, Vec normal, double n1, double n2) {

		// Snell's law: n1*sin(theta1) = n2*sin(theta2)
		// https://en.wikipedia.org/wiki/Snell%27s_law
		// Presentation slide 73

		// If n1==n2 there isn't a refraction
		if (n1 == n2) return u;

		double hitAngle = Ops.dot(Ops.neg(u), normal);
		hitAngle = Math.pow(hitAngle, 2);

		// When light travels from a medium with a higher refractive index to one with a lower refractive index,
		// Snell's law seems to require in some cases (whenever the angle of incidence is large enough)
		// that the sine of the angle of refraction be greater than one.
		if (n1 > n2){
			// There's no refraction at angels grater than the critical angle, ray reflects back
			// n2 / n1 - critical angle
			double criticalAngle = Math.pow((n2 / n1), 2);
			if (1 - hitAngle >= criticalAngle)
				return Ops.reflect(u, normal);
		}

		// Ray is refracted
		// http://shaderbits.com/blog/optimized-snell-s-law-refraction/
		// We need to calculate x and normalize it
		// y is the normal we're given
		Vec x = Ops.add(u, Ops.mult(Ops.dot(Ops.neg(u), normal), normal)).normalize();
		double sinTetha = Math.pow(n1, 2) * (1.0 - hitAngle) / Math.pow(n2, 2);
		Vec sinXform = Ops.mult(Math.sqrt(sinTetha), x);
		Vec cosYform = Ops.mult(- Math.sqrt(1 - sinTetha), normal);
		return Ops.add(sinXform, cosYform);
	}
}
